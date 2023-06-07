package com.ringo.service.company;

import com.ringo.dto.ReviewPageRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.dto.company.ReviewRequestDto;
import com.ringo.dto.company.ReviewResponseDto;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.OrganisationMapper;
import com.ringo.mapper.company.ReviewMapper;
import com.ringo.model.common.AbstractEntity;
import com.ringo.model.company.Organisation;
import com.ringo.model.company.Participant;
import com.ringo.model.company.Review;
import com.ringo.repository.OrganisationRepository;
import com.ringo.repository.ParticipantRepository;
import com.ringo.repository.ReviewRepository;
import com.ringo.service.security.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReviewService {

    private final OrganisationRepository organisationRepository;
    private final ParticipantRepository participantRepository;
    private final UserService userService;
    private final ReviewRepository repository;
    private final ReviewMapper reviewMapper;
    private final OrganisationMapper organisationMapper;

    public OrganisationResponseDto createReview(Long id, ReviewRequestDto dto) {
        log.info("rateOrganisation: {}", id);

        Organisation organisation = organisationRepository.findByIdWithEvents(id).orElseThrow(
                () -> new UserException("Organisation#" + id + " was not found")
        );

        Participant participant = participantRepository.findById(userService.getCurrentUserAsEntity().getId()).orElseThrow(
                () -> new UserException("Current user is not a participant")
        );

        if(repository.existsByOrganisationAndParticipant(organisation, participant))
            throw new UserException("Current user has already rated this organisation");

        Review review = reviewMapper.toEntity(dto);
        review.setParticipant(participant);
        review.setOrganisation(organisation);
        review.setCreatedAt(LocalDateTime.now());

        review = repository.save(review);
        updateRating(organisation);

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto updateReview(Long id, ReviewRequestDto dto) {
        log.info("updateReview: {}", dto.getId());

        Review review = repository.findById(dto.getId()).orElseThrow(
                () -> new UserException("Review#" + dto.getId() + " was not found")
        );

        Participant participant = participantRepository.findById(userService.getCurrentUserAsEntity().getId()).orElseThrow(
                () -> new UserException("Current user is not a participant")
        );

        if(!Objects.equals(review.getParticipant().getId(), participant.getId()))
            throw new UserException("Current user is not the author of this review");

        review.setRate(dto.getRate());
        review.setComment(dto.getComment());
        review = repository.save(review);

        updateRating(review.getOrganisation());

        Organisation organisation = organisationRepository.findByIdWithEvents(review.getOrganisation().getId()).orElseThrow(
                () -> new UserException("Organisation#" +  id + " was not found")
        );

        return organisationMapper.toDto(organisation);
    }

    public OrganisationResponseDto deleteReview(Long id) {
        log.info("deleteReview: {}", id);
        Review review = repository.findById(id).orElseThrow(
                () -> new UserException("Review#" + id + " was not found")
        );

        Participant participant = participantRepository.findById(userService.getCurrentUserAsEntity().getId()).orElseThrow(
                () -> new UserException("Current user is not a participant")
        );

        if(!Objects.equals(review.getParticipant().getId(), participant.getId()))
            throw new UserException("Current user is not the author of this review");

        repository.delete(review);
        updateRating(review.getOrganisation());

        Organisation organisation = organisationRepository.findByIdWithEvents(review.getOrganisation().getId()).orElseThrow(
                () -> new UserException("Organisation#" + review.getOrganisation().getId() + " was not found")
        );

        return organisationMapper.toDto(organisation);
    }

    public List<ReviewResponseDto> findAllByOrganisation(Long organisationId, ReviewPageRequestDto request) {
        Optional<Participant> participantOptional = participantRepository.findById(userService.getCurrentUserAsEntity().getId());

        Page<Review> page = repository.findAll(
                request.getSpecification(
                        organisationId,
                        participantOptional.map(AbstractEntity::getId).orElse(null)
                ),
                request.getPageable()
        );
        return reviewMapper.toDtos(page.getContent());
    }

    private void updateRating(Organisation organisation) {
        organisation.setRating(repository.getAverageRatingByOrganisationId(organisation.getId()));
        organisationRepository.save(organisation);
    }
}
