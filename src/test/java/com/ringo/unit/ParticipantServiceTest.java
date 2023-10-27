package com.ringo.unit;

import com.ringo.auth.AuthenticationService;
import com.ringo.auth.IdProvider;
import com.ringo.dto.company.request.ParticipantRequestDto;
import com.ringo.dto.company.response.ParticipantResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.ParticipantMapper;
import com.ringo.mapper.company.ParticipantMapperImpl;
import com.ringo.mock.common.MultipartFileMock;
import com.ringo.mock.dto.ParticipantDtoMock;
import com.ringo.mock.model.ParticipantMock;
import com.ringo.model.company.Organisation;
import com.ringo.model.company.Participant;
import com.ringo.model.photo.Photo;
import com.ringo.model.security.Role;
import com.ringo.repository.JoiningIntentRepository;
import com.ringo.repository.company.ParticipantRepository;
import com.ringo.repository.company.TicketRepository;
import com.ringo.repository.company.UserRepository;
import com.ringo.service.common.PhotoService;
import com.ringo.service.company.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParticipantServiceTest {
    @Mock
    private ParticipantRepository participantRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TicketRepository ticketRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private PhotoService photoService;
    @Mock
    private IdProvider idProvider;
    @Mock
    private JoiningIntentRepository joiningIntentRepository;

    @Captor
    private ArgumentCaptor<Participant> participantCaptor;

    @Spy
    private ParticipantMapper mapper = new ParticipantMapperImpl();
    @InjectMocks
    private ParticipantService service;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(service, "passwordEncoder", new BCryptPasswordEncoder());
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "abstractUserMapper", new ParticipantMapperImpl());
        ReflectionTestUtils.setField(service, "repository", participantRepository);
        ReflectionTestUtils.setField(service, "ticketRepository", ticketRepository);
    }

    @Test
    void findByIdSuccess() {
        // given
        Long id = 1L;
        Participant expected = ParticipantMock.getParticipantMock();
        expected.setId(id);
        // when
        when(participantRepository.findByIdActive(id)).thenReturn(Optional.of(expected));
        // then
        ParticipantResponseDto responseDto = service.findById(id);
        // assert
        assertThat(responseDto).isNotNull();
        ParticipantResponseDto expectedDto = mapper.toDto(expected);

        assertThat(responseDto).isEqualTo(expectedDto);
        verify(participantRepository, times(1)).findByIdActive(id);
    }

    @Test
    void findByIdNotFound() {
        // given
        Long id = 1L;
        // when
        when(participantRepository.findByIdActive(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.findById(id));
    }

    @Test
    void findCurrentParticipant() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));

        //then
        ParticipantResponseDto responseDto = service.findCurrentParticipant();
        assertThat(responseDto).isNotNull();
        ParticipantResponseDto expectedDto = mapper.toDto(participant);
        assertThat(responseDto).isEqualTo(expectedDto);
    }

    @Test
    void saveSuccess() {
        // given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setIsActive(false);
        participant.setEmailVerified(false);
        participant.setWithIdProvider(false);

        ParticipantRequestDto dto = ParticipantDtoMock.getParticipantMockDto();
        dto.setEmail(participant.getEmail());
        dto.setUsername(participant.getUsername());
        // when
        when(participantRepository.save(participantCaptor.capture())).thenReturn(participant);
        when(userRepository.findVerifiedByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        // then
        ParticipantResponseDto responseDto = service.save(dto);
        // assert
        Participant saved = participantCaptor.getAllValues().get(0);
        assertThat(saved).isNotNull();
        assertThat(saved).usingRecursiveComparison().ignoringFields("createdAt", "id", "password").isEqualTo(participant);
        assertThat(saved.getPassword()).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo(participant.getPassword());
        assertThat(saved.getCreatedAt().atZone(ZoneId.systemDefault()).getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());
        assertThat(saved.getCreatedAt().atZone(ZoneId.systemDefault()).getYear()).isEqualTo(LocalDate.now().getYear());
        assertThat(saved.getUpdatedAt()).isNull();
        assertThat(saved.getWithIdProvider()).isFalse();

        assertThat(responseDto).isNotNull();
        ParticipantResponseDto expectedDto = mapper.toDto(participant);

        assertThat(responseDto).isEqualTo(expectedDto);
        verify(authenticationService, times(1)).sendVerificationEmail(participantCaptor.capture());
        verify(participantRepository, times(1)).save(saved);

        assertThat(participantCaptor.getAllValues().get(1).getEmail()).isEqualTo(saved.getEmail());
    }

    @Test
    void signUpWithIdProviderSuccess() {
        //given
        String idToken = "idToken";
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setEmailVerified(true);
        participant.setWithIdProvider(true);

        //when
        when(participantRepository.save(participantCaptor.capture())).thenReturn(participant);
        when(userRepository.findVerifiedByEmail(participant.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByUsername(participant.getUsername())).thenReturn(Optional.empty());
        when(idProvider.getUserFromToken(idToken)).thenReturn(participant);

        //then
        ParticipantResponseDto responseDto = service.signUpWithIdProvider(idToken, idProvider, Role.ROLE_PARTICIPANT);
        assertThat(responseDto).isEqualTo(mapper.toDto(participant));

        Participant saved = participantCaptor.getValue();
        assertThat(saved.getUsername()).startsWith("user");
        assertThat(saved.getEmail()).isEqualTo(participant.getEmail());
        assertThat(saved.getIsActive()).isFalse();
        assertThat(saved.getEmailVerified()).isTrue();
        assertThat(saved.getWithIdProvider()).isTrue();
    }

    @Test
    void signUpWithIdProviderEmailTaken() {
        //given
        String idToken = "idToken";
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(userRepository.findByUsername(participant.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findVerifiedByEmail(participant.getEmail())).thenReturn(Optional.of(Organisation.builder().id(participant.getId() + 1).build()));
        when(idProvider.getUserFromToken(idToken)).thenReturn(participant);

        //then
        assertThrows(UserException.class, () -> service.signUpWithIdProvider(idToken, idProvider, Role.ROLE_PARTICIPANT));
    }

    @Test
    void partialUpdateSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        ParticipantRequestDto dto = ParticipantDtoMock.getParticipantMockDto();
        dto.setEmail(participant.getEmail());
        dto.setUsername(participant.getUsername());
        //when
        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findVerifiedByEmail(participant.getEmail())).thenReturn(Optional.empty());
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));
        when(participantRepository.save(participantCaptor.capture())).thenReturn(participant);
        //then
        ParticipantResponseDto responseDto = service.partialUpdate(dto);

        Participant saved = participantCaptor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved).usingRecursiveComparison().ignoringFields("updatedAt").isEqualTo(participant);
        assertThat(saved.getUpdatedAt().atZone(ZoneId.systemDefault()).getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());
        assertThat(saved.getUpdatedAt().atZone(ZoneId.systemDefault()).getYear()).isEqualTo(LocalDate.now().getYear());

        assertThat(responseDto).isNotNull();
        ParticipantResponseDto expectedDto = mapper.toDto(participant);

        assertThat(responseDto).isEqualTo(expectedDto);

        verify(participantRepository, times(1)).save(any(Participant.class));
    }

    @Test
    void getFullUserSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullActiveById(participant.getId())).thenReturn(Optional.of(participant));

        //then
        Participant returned = service.getFullActiveUser();
        assertThat(returned).isEqualTo(participant);
    }

    @Test
    void getFullUserNotFound() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullActiveById(participant.getId())).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> service.getFullActiveUser());
    }

    @Test
    void throwIfRequiredFieldsNotFilledNoUsername() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setUsername(null);

        //then
        assertThrows(UserException.class, () -> service.throwIfRequiredFieldsNotFilled(participant));
    }

    @Test
    void throwIfRequiredFieldsNotFilledNoName() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setName(null);

        //then
        assertThrows(UserException.class, () -> service.throwIfRequiredFieldsNotFilled(participant));
    }

    @Test
    void throwIfRequiredFieldsNotFilledNoGender() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setGender(null);

        //then
        assertThrows(UserException.class, () -> service.throwIfRequiredFieldsNotFilled(participant));
    }

    @Test
    void throwIfRequiredFieldsNotFilledNoDateOfBirth() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setDateOfBirth(null);

        //then
        assertThrows(UserException.class, () -> service.throwIfRequiredFieldsNotFilled(participant));
    }

    @Test
    void throwIfUniqueConstraintsViolatedEmailTaken() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setId(System.currentTimeMillis() - 100);
        Participant found = ParticipantMock.getParticipantMock();
        found.setId(System.currentTimeMillis());
        //when
        when(userRepository.findVerifiedByEmail(participant.getEmail())).thenReturn(Optional.of(found));
        when(userRepository.findByUsername(participant.getUsername())).thenReturn(Optional.empty());
        //then
        assertThrows(UserException.class, () -> service.throwIfUniqueConstraintsViolated(participant));
    }

    @Test
    void throwIfUniqueConstraintsViolatedUsernameTaken() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setId(System.currentTimeMillis() - 100);
        Participant found = ParticipantMock.getParticipantMock();
        found.setId(System.currentTimeMillis());
        //when
        when(userRepository.findByUsername(participant.getUsername())).thenReturn(Optional.of(found));
        //then
        assertThrows(UserException.class, () -> service.throwIfUniqueConstraintsViolated(participant));
    }

    @Test
    void deleteSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));
        when(ticketRepository.findAllByParticipantId(participant.getId())).thenReturn(new ArrayList<>());
        //then
        service.delete();
        verify(participantRepository, times(1)).delete(any(Participant.class));
    }

    @Test
    void activateSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setIsActive(false);
        participant.setEmailVerified(true);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.save(participantCaptor.capture())).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));
        //then
        ParticipantResponseDto activated = service.activate();
        assertThat(activated).isNotNull();
        assertThat(activated.getIsActive()).isTrue();
        assertThat(activated).usingRecursiveComparison().ignoringFields("isActive").isEqualTo(mapper.toDto(participant));
        verify(participantRepository, times(1)).save(any(Participant.class));
    }

    @Test
    void activateAlreadyActive() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setIsActive(true);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));
        //then
        assertThrows(UserException.class, () -> service.activate());
    }

    @Test
    void activateEmailNotVerified() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setIsActive(false);
        participant.setEmailVerified(false);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));

        //then
        assertThrows(UserException.class, () -> service.activate());
    }

    @Test
    void activateRequiredFieldsNotFilled() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setIsActive(false);
        participant.setName(null);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));
        //then
        assertThrows(UserException.class, () -> service.activate());
    }

    @Test
    void setPhotoSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        MultipartFile file = MultipartFileMock.getMockMultipartFile();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullActiveById(participant.getId())).thenReturn(Optional.of(participant));
        when(participantRepository.save(participant)).thenReturn(participant);

        //then
        ParticipantResponseDto dto = service.setPhoto(file);
        assertThat(dto).isEqualTo(mapper.toDto(participant));

        verify(participantRepository, times(1)).save(any(Participant.class));

        try {
            verify(photoService, times(1)).save("profilePictures/user#" + participant.getId(), "png", file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void removePhotoSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        Photo photo = new Photo();
        photo.setId(System.currentTimeMillis());
        participant.setProfilePicture(photo);

        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullActiveById(participant.getId())).thenReturn(Optional.of(participant));
        when(participantRepository.save(participant)).thenReturn(participant);

        //then
        ParticipantResponseDto dto = service.removePhoto();
        assertThat(dto).isEqualTo(mapper.toDto(participant));

        verify(participantRepository, times(1)).save(any(Participant.class));
        verify(photoService, times(1)).delete(photo.getId());
    }

    @Test
    void removePhotoAbsent() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setProfilePicture(null);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullActiveById(participant.getId())).thenReturn(Optional.of(participant));

        //then
        assertThrows(UserException.class, () -> service.removePhoto());

        verify(photoService, never()).delete(anyLong());
    }
}
