package com.project_x.listing.validation;

import com.project_x.adress.repository.LgaRepository;
import com.project_x.adress.repository.StateRepository;
import com.project_x.core.exception.BadRequestException;
import com.project_x.listing.dto.request.PublicPropertyFilterRequest;
import com.project_x.listing.repository.PropertyTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PublicPropertyFilterValidator {

    private static final long DEFAULT_PAGE = 1L;
    private static final long DEFAULT_PAGE_SIZE = 20L;
    private static final long MAX_PAGE_SIZE = 35L;

    private static final Set<Integer> ALLOWED_BEDROOM_COUNTS = Set.of(0, 1, 2, 3, 4, 5);
    private static final Set<Integer> ALLOWED_BATHROOM_COUNTS = Set.of(1, 2, 3, 4, 5);
    private static final Set<Integer> ALLOWED_TOILET_COUNTS = Set.of(1, 2, 3, 4);

    private final StateRepository stateRepository;
    private final LgaRepository lgaRepository;
    private final PropertyTypeRepository propertyTypeRepository;

    public void validateAndNormalize(PublicPropertyFilterRequest filter) {
        normalizePagination(filter);
        normalizeNeighbourhood(filter);
        validatePagination(filter);
        validateLocation(filter);
        validatePropertyTypes(filter);
        validateCountSelections(filter);
        validateRentRange(filter);
    }

    private void normalizePagination(PublicPropertyFilterRequest filter) {
        if (filter.getPage() == null) {
            filter.setPage(DEFAULT_PAGE);
        }

        if (filter.getPageSize() == null) {
            filter.setPageSize(DEFAULT_PAGE_SIZE);
        }
    }

    private void normalizeNeighbourhood(PublicPropertyFilterRequest filter) {
        String neighbourhood = filter.getNeighbourhood();

        filter.setNeighbourhood(
                neighbourhood == null || neighbourhood.isBlank()
                        ? null
                        : neighbourhood.trim()
        );
    }

    private void validatePagination(PublicPropertyFilterRequest filter) {
        if (filter.getPage() < 1) {
            throw new BadRequestException("Page must be at least 1");
        }

        if (filter.getPageSize() < 1 || filter.getPageSize() > MAX_PAGE_SIZE) {
            throw new BadRequestException(
                    "Page size must be between 1 and " + MAX_PAGE_SIZE
            );
        }
    }

    private void validateLocation(PublicPropertyFilterRequest filter) {
        if (filter.getLgaId() != null && filter.getStateId() == null) {
            throw new BadRequestException("Select a state before selecting a local government area");
        }

        if (filter.getNeighbourhood() != null && filter.getLgaId() == null) {
            throw new BadRequestException("Select a local government area before entering a neighbourhood");
        }

        if (
                filter.getStateId() != null && !stateRepository.existsById(filter.getStateId())
        ) {
            throw new BadRequestException("The selected state does not exist");
        }

        if (
                filter.getLgaId() != null && !lgaRepository.existsByIdAndState_Id(filter.getLgaId(), filter.getStateId()
                        )
        ) {
            throw new BadRequestException("The selected local government area does not belong to the selected state");
        }
    }

    private void validatePropertyTypes(PublicPropertyFilterRequest filter) {
        if (!hasValues(filter.getPropertyTypeIds())) {
            return;
        }

        if (filter.getPropertyTypeIds().contains(null)) {
            throw new BadRequestException(
                    "Property type selections must contain valid IDs"
            );
        }

        long activePropertyTypeCount =
                propertyTypeRepository.countByIdInAndActiveTrue(
                        filter.getPropertyTypeIds()
                );

        if (activePropertyTypeCount != filter.getPropertyTypeIds().size()) {
            throw new BadRequestException(
                    "One or more selected property types do not exist or are inactive"
            );
        }
    }

    private void validateCountSelections(PublicPropertyFilterRequest filter) {
        validateCounts(
                "Bedroom",
                filter.getBedroomCounts(),
                ALLOWED_BEDROOM_COUNTS,
                "0, 1, 2, 3, 4, or 5+"
        );
        validateCounts(
                "Bathroom",
                filter.getBathroomCounts(),
                ALLOWED_BATHROOM_COUNTS,
                "1, 2, 3, 4, or 5+"
        );
        validateCounts(
                "Toilet",
                filter.getToiletCounts(),
                ALLOWED_TOILET_COUNTS,
                "1, 2, 3, or 4+"
        );
    }

    private void validateCounts(
            String label,
            Set<Integer> selectedCounts,
            Set<Integer> allowedCounts,
            String acceptedValues
    ) {
        if (!hasValues(selectedCounts)) {
            return;
        }

        if (!allowedCounts.containsAll(selectedCounts)) {
            throw new BadRequestException(label + " count must be one of: " + acceptedValues);
        }
    }

    private void validateRentRange(PublicPropertyFilterRequest filter) {
        if (isNegative(filter.getMinRent()) || isNegative(filter.getMaxRent())) {
            throw new BadRequestException("Rent values cannot be negative");
        }

        if (filter.getMinRent() != null && filter.getMaxRent() != null && filter.getMinRent().compareTo(filter.getMaxRent()) > 0) {
            throw new BadRequestException("Minimum rent cannot be greater than maximum rent");
        }
    }

    private boolean isNegative(BigDecimal value) {
        return value != null && value.signum() < 0;
    }

    private boolean hasValues(Set<?> values) {
        return values != null && !values.isEmpty();
    }
}
