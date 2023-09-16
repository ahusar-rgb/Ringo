package com.ringo.unit;

import com.ringo.dto.ReviewPageRequestDto;
import com.ringo.dto.company.request.ReviewRequestDto;
import com.ringo.dto.company.response.OrganisationResponseDto;
import com.ringo.dto.company.response.ReviewResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.*;
import com.ringo.mock.dto.ReviewDtoMock;
import com.ringo.mock.model.OrganisationMock;
import com.ringo.mock.model.ParticipantMock;
import com.ringo.mock.model.ReviewMock;
import com.ringo.model.company.Organisation;
import com.ringo.model.company.Participant;
import com.ringo.model.company.Review;
import com.ringo.repository.company.OrganisationRepository;
import com.ringo.repository.company.ReviewRepository;
import com.ringo.service.company.ParticipantService;
import com.ringo.service.company.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private ParticipantService participantService;
    @Mock
    private OrganisationRepository organisationRepository;
    @InjectMocks
    private ReviewService reviewService;
    @Captor
    private ArgumentCaptor<Review> reviewCaptor;
    @Spy
    private ReviewMapper reviewMapper;
    @Captor
    private ArgumentCaptor<Organisation> organisationCaptor;


    @BeforeEach
    void setUp() {
        OrganisationMapper organisationMapper = new OrganisationMapperImpl();
        ReflectionTestUtils.setField(organisationMapper, "labelMapper", new LabelMapperImpl());

        reviewMapper = new ReviewMapperImpl();
        ReflectionTestUtils.setField(reviewMapper, "participantMapper", new ParticipantMapperImpl());

        ReflectionTestUtils.setField(reviewService, "organisationMapper", organisationMapper);
        ReflectionTestUtils.setField(reviewService, "reviewMapper", reviewMapper);
    }

    @Test
    void createSuccess() {
        // given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        final float organisationRating = 5.0f;
        Participant participant = ParticipantMock.getParticipantMock();

        Review review = ReviewMock.getReviewMock();
        review.setParticipant(participant);
        review.setOrganisation(organisation);

        ReviewRequestDto dto = ReviewDtoMock.getReviewDtoMock();

        // when
        when(organisationRepository.findByIdActiveWithEvents(organisation.getId())).thenReturn(Optional.of(organisation));
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(reviewRepository.existsByOrganisationAndParticipant(organisation, participant)).thenReturn(false);
        when(reviewRepository.getAverageRatingByOrganisationId(organisation.getId())).thenReturn(organisationRating);
        when(organisationRepository.save(organisation)).thenReturn(organisation);

        // then
        OrganisationResponseDto responseDto = reviewService.createReview(organisation.getId(), dto);

        verify(reviewRepository, times(1)).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertThat(savedReview).usingRecursiveComparison().ignoringFields("id", "participant", "organisation", "createdAt").isEqualTo(review);
        assertThat(savedReview.getCreatedAt()).isNotNull();
        verify(organisationRepository, times(1)).save(organisationCaptor.capture());
        Organisation savedOrganisation = organisationCaptor.getValue();
        assertThat(savedOrganisation).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);
        assertThat(savedOrganisation.getRating()).isEqualTo(organisationRating);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo(organisationRating);
    }

    @Test
    void createAlreadyExists() {
        //when
        Organisation organisation = OrganisationMock.getOrganisationMock();
        Participant participant = ParticipantMock.getParticipantMock();
        ReviewRequestDto dto = ReviewDtoMock.getReviewDtoMock();
        //when
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(organisationRepository.findByIdActiveWithEvents(organisation.getId())).thenReturn(Optional.of(organisation));
        when(reviewRepository.existsByOrganisationAndParticipant(organisation, participant)).thenReturn(true);
        //then
        assertThrows(UserException.class, () -> reviewService.createReview(organisation.getId(), dto));

        verify(reviewRepository, times(0)).save(any());
        verify(organisationRepository, times(0)).save(any());
    }

    @Test
    void createInvalidRate() {
        //TODO: Add validation
    }

    @Test
    void updateSuccess() {
        //given
        Review review = ReviewMock.getReviewMock();
        Participant participant = ParticipantMock.getParticipantMock();
        review.setParticipant(participant);
        Organisation organisation = OrganisationMock.getOrganisationMock();
        final float organisationRating = 5.0f;
        review.setOrganisation(organisation);
        ReviewRequestDto dto = ReviewDtoMock.getReviewDtoMock();
        //when
        when(organisationRepository.findActiveById(organisation.getId())).thenReturn(Optional.of(organisation));
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(reviewRepository.findByOrganisationAndParticipant(organisation, participant)).thenReturn(Optional.of(review));
        when(organisationRepository.save(organisation)).thenReturn(organisation);
        when(reviewRepository.getAverageRatingByOrganisationId(organisation.getId())).thenReturn(organisationRating);
        //then
        OrganisationResponseDto responseDto = reviewService.updateReview(organisation.getId(), dto);

        verify(reviewRepository, times(1)).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertThat(savedReview.getComment()).isEqualTo(dto.getComment());
        assertThat(savedReview.getRate()).isEqualTo(dto.getRate());
        assertThat(savedReview.getUpdatedAt()).isNotNull();

        verify(organisationRepository, times(1)).save(organisationCaptor.capture());
        Organisation savedOrganisation = organisationCaptor.getValue();
        assertThat(savedOrganisation).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);
        assertThat(savedOrganisation.getRating()).isEqualTo(organisationRating);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo(organisationRating);
    }

    @Test
    void partialUpdateSuccess() {
        //given
        Review review = ReviewMock.getReviewMock();
        Participant participant = ParticipantMock.getParticipantMock();
        review.setParticipant(participant);
        final String oldComment = "old comment";
        review.setComment(oldComment);
        Organisation organisation = OrganisationMock.getOrganisationMock();
        final float organisationRating = 5.0f;
        review.setOrganisation(organisation);
        ReviewRequestDto dto = ReviewDtoMock.getReviewDtoMock();
        dto.setComment(null);
        //when
        when(organisationRepository.findActiveById(organisation.getId())).thenReturn(Optional.of(organisation));
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(reviewRepository.findByOrganisationAndParticipant(organisation, participant)).thenReturn(Optional.of(review));
        when(organisationRepository.save(organisation)).thenReturn(organisation);
        when(reviewRepository.getAverageRatingByOrganisationId(organisation.getId())).thenReturn(organisationRating);
        //then
        OrganisationResponseDto responseDto = reviewService.updateReview(organisation.getId(), dto);

        verify(reviewRepository, times(1)).save(reviewCaptor.capture());
        Review savedReview = reviewCaptor.getValue();
        assertThat(savedReview.getComment()).isEqualTo(oldComment);
        assertThat(savedReview.getRate()).isEqualTo(dto.getRate());
        assertThat(savedReview.getUpdatedAt()).isNotNull();

        verify(organisationRepository, times(1)).save(organisationCaptor.capture());
        Organisation savedOrganisation = organisationCaptor.getValue();
        assertThat(savedOrganisation).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);
        assertThat(savedOrganisation.getRating()).isEqualTo(organisationRating);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo(organisationRating);
    }

    @Test
    void updateReviewNotFound() {
        //given
        Review review = ReviewMock.getReviewMock();
        Participant participant = ParticipantMock.getParticipantMock();
        review.setParticipant(participant);
        Organisation organisation = OrganisationMock.getOrganisationMock();
        ReviewRequestDto dto = ReviewDtoMock.getReviewDtoMock();
        //when
        when(organisationRepository.findActiveById(organisation.getId())).thenReturn(Optional.of(organisation));
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(reviewRepository.findByOrganisationAndParticipant(organisation, participant)).thenReturn(Optional.empty());
        //then
        assertThrows(NotFoundException.class, () -> reviewService.updateReview(organisation.getId(), dto));

        verify(reviewRepository, times(0)).save(any());
        verify(organisationRepository, times(0)).save(any());
    }

    @Test
    void deleteSuccess() {
        //given
        Review review = ReviewMock.getReviewMock();
        Participant participant = ParticipantMock.getParticipantMock();
        review.setParticipant(participant);
        Organisation organisation = OrganisationMock.getOrganisationMock();
        final float organisationRating = 5.0f;
        review.setOrganisation(organisation);

        //when
        when(organisationRepository.findActiveById(organisation.getId())).thenReturn(Optional.of(organisation));
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(reviewRepository.findByOrganisationAndParticipant(organisation, participant)).thenReturn(Optional.of(review));
        when(organisationRepository.save(organisation)).thenReturn(organisation);
        when(reviewRepository.getAverageRatingByOrganisationId(organisation.getId())).thenReturn(organisationRating);


        //then
        OrganisationResponseDto responseDto = reviewService.deleteReview(organisation.getId());

        verify(reviewRepository, times(1)).delete(review);
        verify(organisationRepository, times(1)).save(organisationCaptor.capture());

        Organisation savedOrganisation = organisationCaptor.getValue();
        assertThat(savedOrganisation).usingRecursiveComparison().ignoringFields("rating").isEqualTo(organisation);
        assertThat(savedOrganisation.getRating()).isEqualTo(organisationRating);

        assertThat(responseDto).isNotNull();
        assertThat(responseDto.getRating()).isEqualTo(organisationRating);
    }

    @Test
    void deleteNotFound() {
        //given
        final Long id = System.currentTimeMillis();

        Organisation organisation = OrganisationMock.getOrganisationMock();
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(organisationRepository.findActiveById(id)).thenReturn(Optional.of(organisation));
        when(participantService.getFullActiveUser()).thenReturn(participant);
        when(reviewRepository.findByOrganisationAndParticipant(organisation, participant)).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> reviewService.deleteReview(id));
    }

    @Test
    void findAllByOrganisationSuccess() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        Participant participant = ParticipantMock.getParticipantMock();

        ReviewPageRequestDto request = new ReviewPageRequestDto();
        request.setPage(0);
        request.setSize(10);

        List<Review> reviews = List.of(
                Review.builder()
                        .id(1L)
                        .participant(participant)
                        .organisation(organisation)
                        .comment("comment1")
                        .rate(1)
                        .build(),
                Review.builder().id(2L)
                        .participant(
                                Participant.builder()
                                        .id(System.currentTimeMillis())
                                        .build()
                        )
                        .organisation(organisation)
                        .comment("comment2")
                        .rate(2)
                        .build(),
                Review.builder().id(3L)
                        .participant(
                                Participant.builder()
                                        .id(System.currentTimeMillis())
                                        .build()
                        )
                        .organisation(organisation)
                        .comment("comment3")
                        .rate(3)
                        .build()
        );

        //when
        when(reviewRepository.findAll(any(Specification.class), eq(request.getPageable())))
                .thenReturn(new PageImpl<>(reviews, request.getPageable(), reviews.size()));

        //then
        List<ReviewResponseDto> response = reviewService.findAllByOrganisation(organisation.getId(), request);
        assertThat(response).usingRecursiveComparison().isEqualTo(reviewMapper.toDtoList(reviews));
    }
}
