package com.ringo.service.company;

import com.ringo.dto.ReviewPageRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.dto.company.ReviewRequestDto;
import com.ringo.dto.company.ReviewResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.OrganisationMapper;
import com.ringo.mapper.company.ReviewMapper;
import com.ringo.model.company.Organisation;
import com.ringo.model.company.Participant;
import com.ringo.model.company.Review;
import com.ringo.repository.OrganisationRepository;
import com.ringo.repository.ReviewRepository;
import com.ringo.service.time.Time;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final OrganisationRepository organisationRepository;
    private final ParticipantService participantService;
    private final ReviewRepository repository;
    private final ReviewMapper reviewMapper;
    private final OrganisationMapper organisationMapper;

    public OrganisationResponseDto createReview(Long id, ReviewRequestDto dto) {
        log.info("rateOrganisation: {}", id);

        Organisation organisation = organisationRepository.findByIdActiveWithEvents(id).orElseThrow(
                () -> new NotFoundException("Organisation#" + id + " was not found")
        );

        Participant participant = participantService.getFullActiveUser();

        if(repository.existsByOrganisationAndParticipant(organisation, participant))
            throw new UserException("Current user has already rated this organisation");

        Review review = reviewMapper.toEntity(dto);
        review.setParticipant(participant);
        review.setOrganisation(organisation);
        review.setCreatedAt(Time.getLocalUTC());

        repository.save(review);
        organisation = updateRating(organisation);

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto updateReview(Long organisationId, ReviewRequestDto dto) {
        log.info("updateReview(organisaitonId): {}", organisationId);

        Organisation organisation = organisationRepository.findActiveById(organisationId).orElseThrow(
                () -> new NotFoundException("Organisation#" + organisationId + " not found")
        );

        Participant participant = participantService.getFullActiveUser();

        Review review = repository.findByOrganisationAndParticipant(organisation, participant).orElseThrow(
                () -> new NotFoundException("There is no review for this organisation made by current user")
        );

        if(dto.getRate() != null)
            review.setRate(dto.getRate());
        if(dto.getComment() != null)
            review.setComment(dto.getComment());

        review.setUpdatedAt(Time.getLocalUTC());
        repository.save(review);

        organisation = updateRating(organisation);
        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto deleteReview(Long organisationId) {
        log.info("deleteReview: {}", organisationId);

        Organisation organisation = organisationRepository.findActiveById(organisationId).orElseThrow(
                () -> new NotFoundException("Organisation#" + organisationId + " not found")
        );

        Participant participant = participantService.getFullActiveUser();


        Review review = repository.findByOrganisationAndParticipant(organisation, participant).orElseThrow(
                () -> new NotFoundException("There is no review for this organisation made by current user")
        );

        repository.delete(review);

        organisation = updateRating(review.getOrganisation());
        return organisationMapper.toDto(organisation);
    }

    public List<ReviewResponseDto> findAllByOrganisation(Long organisationId, ReviewPageRequestDto request) {
        Participant participant = null;
        try {
            participant = participantService.getFullActiveUser();
        } catch (NotFoundException | UserException ignored) {}

        Page<Review> page = repository.findAll(
                request.getSpecification(
                        organisationId,
                        participant != null ? participant.getId() : null
                ),
                request.getPageable()
        );
        return reviewMapper.toDtoList(page.getContent());
    }

    private Organisation updateRating(Organisation organisation) {
        organisation.setRating(repository.getAverageRatingByOrganisationId(organisation.getId()));
        return organisationRepository.save(organisation);
    }
}
