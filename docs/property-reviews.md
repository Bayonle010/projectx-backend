# Property reviews API

Base URL: `/api/v1`

Guests can read reviews, rating summaries, comments, like counts, and comment
counts. Authentication is required only when creating, editing, deleting, or
liking content. For guest responses, `likedByCurrentUser` is always `false`.

## Public endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/reviews/listings/{listingId}?page=1&pageSize=20` | Paginated reviews for a property |
| `GET` | `/reviews/listings/{listingId}/summary` | Review count, average, and category averages |
| `GET` | `/reviews/{reviewId}` | One review with rating breakdown and counts |
| `GET` | `/reviews/{reviewId}/comments?page=1&pageSize=20` | Paginated comments for a review |

Pagination is one-based. The default is page `1` with `20` records, and the
backend caps `pageSize` at `35`.

Review responses include the nullable `reviewer.avatarUrl`; comment responses
include the nullable `author.avatarUrl`. When it is `null`, the frontend may
render initials from the returned name. `overallRating` is the 1–5 star value.

## Authenticated review endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/reviews/listings/{listingId}` | Create a review |
| `PUT` | `/reviews/{reviewId}` | Replace a review created by the current user |
| `DELETE` | `/reviews/{reviewId}` | Soft-delete a review created by the current user |
| `PUT` | `/reviews/{reviewId}/likes/me` | Like a review; safe to retry |
| `DELETE` | `/reviews/{reviewId}/likes/me` | Remove the current user's like; safe to retry |

Create and update use the same JSON body:

```json
{
  "overallRating": 4,
  "cleanlinessRating": 5,
  "communicationRating": 4,
  "accuracyRating": 4,
  "valueForMoneyRating": 3,
  "reviewText": "Clean property and responsive owner."
}
```

Every rating is required and must be an integer from `1` to `5`.
`reviewText` is optional and limited to 3,000 characters. Any authenticated
user may review a published property except its owner, and a user can have only
one review record per property.

## Authenticated comment endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `POST` | `/reviews/{reviewId}/comments` | Add a comment |
| `PUT` | `/reviews/comments/{commentId}` | Replace a comment created by the current user |
| `DELETE` | `/reviews/comments/{commentId}` | Soft-delete a comment created by the current user |
| `PUT` | `/reviews/comments/{commentId}/likes/me` | Like a comment; safe to retry |
| `DELETE` | `/reviews/comments/{commentId}/likes/me` | Remove the current user's like; safe to retry |

Create and update use this JSON body:

```json
{
  "content": "Was the property easy to access?"
}
```

`content` is required, cannot be blank, and is limited to 2,000 characters.

## Owner review endpoints

These endpoints require authentication and use the authenticated user as the
property owner.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/owner/reviews/dashboard` | Total properties, reviews, average, and category averages |
| `GET` | `/owner/reviews/properties?search=&page=1&pageSize=20` | Paginated property review summaries |

## Frontend authentication behavior

- Public `GET` requests should work without a token and must not redirect a
  guest to login.
- Show the read-only review and comment UI to guests.
- Prompt for login only when a guest attempts a protected action such as add,
  edit, delete, like, or unlike.
- Send `Authorization: Bearer <accessToken>` for every protected request.
- Use `likedByCurrentUser` to render the current user's like state instead of
  inferring it from the total count.

## Expected errors

| Status | Meaning |
| --- | --- |
| `400` | Invalid ratings/content or the property is not published |
| `401` | A protected endpoint was called without valid authentication |
| `403` | The user tried to review their property or modify another user's content |
| `404` | The review, comment, or property is unavailable |
| `409` | The user already has a review for that property |

Deleting reviews and comments is a soft deletion. Deleted content is removed
from public reads, but its database record and associated likes/comments remain
preserved.
