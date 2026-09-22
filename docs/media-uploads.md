# Media upload contract

Small image, document, and video uploads can still use `POST /api/v1/files/images`,
`/documents`, and `/videos` with multipart form data. These endpoints require the
authenticated user's JWT, a `folder` query parameter (up to four path segments
containing letters, numbers, `_`, `-`),
and a `file` part. The multipart video route is limited to 10 MiB. The upload
response now also includes `mediaId`. Every endpoint that starts an upload
requires an `Idempotency-Key` header containing a frontend-generated UUID. Keep
the same key when retrying the same selected file; generate a new key when the
user selects a different file. A retry returns the existing ready media instead
of creating another database record or Cloudinary public ID.

For larger videos, keep the existing upload-then-save-listing flow, but send the
video bytes directly from the client to Cloudinary:

1. After the user selects the file, generate an upload-attempt UUID and call
   `POST /api/v1/files/direct/video/authorize?folder=listings&fileName=tour.mp4&fileSize=11589405`
   with the JWT and `Idempotency-Key` header. `fileName` and `fileSize` are
   required for every direct media kind and are bound to that idempotency key.
   The response contains `mediaId`, `uploadUrl`, `apiKey`, `timestamp`,
   `publicId`, `uploadPreset`, `overwrite`, `signature`, `resourceType`, `maximumBytes`, and
   `maximumDurationSeconds`. Never place the Cloudinary API secret in the client.
   The same endpoint supports `image` and `document`; documents also require a
   `fileName` query parameter so their raw Cloudinary public ID retains an
   allowed extension, such as `.pdf`.
2. Check the selected file's size and duration client-side for quick feedback.
   Upload to `uploadUrl` with form fields `api_key` (`apiKey`), `timestamp`,
   `public_id` (`publicId`), `overwrite`, `signature`, `file`, and, when present,
   `upload_preset` (`uploadPreset`). Do not send
   `mediaId`, `maximumBytes`, or `maximumDurationSeconds` to Cloudinary. For files
   above 100 MB, use Cloudinary's chunked client upload flow, retaining the same
   upload ID across chunks and showing progress/retry to the user. Do not send
   the video to the Spring multipart endpoint.
3. After Cloudinary reports success, call
   `POST /api/v1/files/direct/{mediaId}/complete` with the JWT. The backend
   checks Cloudinary's authoritative metadata, including owner-scoped public ID,
   resource type, format, size, and video duration (maximum 360 seconds). It
   returns the normal file response with `mediaId`, `publicId`, and URLs.
   This completion endpoint is already idempotent by `mediaId` and does not use
   an `Idempotency-Key` header.
4. Send the verified `videoPublicId` and `videoUrl` to the draft listing endpoint.
   For images use each verified `publicId` and `optimizedUrl`; for ownership
   documents use the verified `originalUrl`. The listing service looks up the
   asset by public ID or URL and owner, and stores its canonical URL rather than
   trusting client-supplied URLs. Guest listing reads do not need ownership checks.

`MAX_VIDEO_UPLOAD_BYTES` defaults to 524288000 (500 MiB). Set it no higher than
the maximum single asset your Cloudinary account accepts. Spring's global
multipart limit remains 10 MiB per file; raising it to hundreds of megabytes
would not help direct uploads. Clients should retry `/complete` if the upload
finished but the completion request timed out. Unfinished reservations and
unattached Cloudinary assets will require a separate cleanup policy before
opening uploads to a large user base.

Before using the direct video route, create a **signed** Cloudinary upload preset
with `max_file_size` at or below `MAX_VIDEO_UPLOAD_BYTES` and your allowed video
formats, then set `CLOUDINARY_VIDEO_UPLOAD_PRESET` to its name. The backend will
refuse to authorize direct videos until this is configured. It still verifies
actual duration and bytes after upload. The six-minute duration rule is checked
after Cloudinary receives the video, so also reject it early in the browser for
good user experience. The browser check alone is not a security boundary.

## Media attachment lifecycle

The existing listing and amenity request bodies remain supported. They may send
the current public IDs and URLs, but the backend resolves the public ID to an
owned, `READY` `media_assets` record and always persists the canonical URL.
Client-supplied URLs are not trusted.

When verified media is attached to a listing or amenity, the backend records a
`media_asset_usages` relationship containing the media asset, owning entity, and
usage type. The generic file deletion endpoint rejects deletion while any usage
exists. Amenities are shared catalogue records, so deleting one deactivates it
instead of removing its image, usage record, or existing listing relationships.
Inactive amenities are hidden from the selection catalogue and cannot be added
to another listing, while listings that already use them keep working. Legacy
listing and amenity reference checks remain in the deletion service to protect
records created before usage tracking was added.
