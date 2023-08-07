package com.ringo.unit;

import com.ringo.auth.AuthenticationService;
import com.ringo.dto.company.ParticipantRequestDto;
import com.ringo.dto.company.ParticipantResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.ParticipantMapper;
import com.ringo.mapper.company.ParticipantMapperImpl;
import com.ringo.mock.dto.ParticipantDtoMock;
import com.ringo.mock.model.ParticipantMock;
import com.ringo.model.company.Participant;
import com.ringo.model.security.User;
import com.ringo.repository.ParticipantRepository;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.PhotoService;
import com.ringo.service.company.ParticipantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
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
    private AuthenticationService authenticationService;
    @Mock
    private PhotoService photoService;

    @Captor
    private ArgumentCaptor<Participant> participantCaptor;

    @Spy
    private ParticipantMapper mapper = (ParticipantMapper) new ParticipantMapperImpl();
    @InjectMocks
    private ParticipantService service;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(service, "passwordEncoder", new BCryptPasswordEncoder());
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(service, "abstractUserMapper", new ParticipantMapperImpl());
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
        ParticipantRequestDto dto = ParticipantDtoMock.getParticipantMockDto();
        // when
        when(participantRepository.save(participantCaptor.capture())).thenReturn(participant);
        when(userRepository.findActiveByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findActiveByUsername(dto.getUsername())).thenReturn(Optional.empty());
        // then
        ParticipantResponseDto responseDto = service.save(dto);
        // assert
        Participant saved = participantCaptor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved).usingRecursiveComparison().ignoringFields("createdAt", "id", "password").isEqualTo(participant);
        assertThat(saved.getPassword()).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo(participant.getPassword());
        assertThat(saved.getCreatedAt().getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());
        assertThat(saved.getCreatedAt().getYear()).isEqualTo(LocalDate.now().getYear());
        assertThat(saved.getUpdatedAt()).isNull();

        assertThat(responseDto).isNotNull();
        ParticipantResponseDto expectedDto = mapper.toDto(participant);

        assertThat(responseDto).isEqualTo(expectedDto);
        verify(participantRepository, times(1)).save(saved);
    }

    @Test
    void partialUpdateSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        ParticipantRequestDto dto = ParticipantDtoMock.getParticipantMockDto();
        //when
        when(userRepository.findActiveByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findActiveByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));
        when(participantRepository.save(participantCaptor.capture())).thenReturn(participant);
        //then
        ParticipantResponseDto responseDto = service.partialUpdate(dto);

        Participant saved = participantCaptor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved).usingRecursiveComparison().ignoringFields("updatedAt").isEqualTo(participant);
        assertThat(saved.getUpdatedAt().getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());
        assertThat(saved.getUpdatedAt().getYear()).isEqualTo(LocalDate.now().getYear());

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
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.of(participant));

        //then
        Participant returned = service.getFullUser();
        assertThat(returned).isEqualTo(participant);
    }

    @Test
    void getFullUserNotFound() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        when(participantRepository.findFullById(participant.getId())).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> service.getFullUser());
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
        when(userRepository.findActiveByEmail(participant.getEmail())).thenReturn(Optional.of(found));
        when(userRepository.findActiveByUsername(participant.getUsername())).thenReturn(Optional.empty());
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
        when(userRepository.findActiveByUsername(participant.getUsername())).thenReturn(Optional.of(found));
        //then
        assertThrows(UserException.class, () -> service.throwIfUniqueConstraintsViolated(participant));
    }

    @Test
    void deleteSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        //when
        when(authenticationService.getCurrentUser()).thenReturn(participant);
        //then
        service.delete();
        verify(participantRepository, times(1)).delete(any(Participant.class));
    }

    @Test
    void activateSuccess() {
        //given
        Participant participant = ParticipantMock.getParticipantMock();
        participant.setIsActive(false);
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

    }

    @Test
    void removePhotoSuccess() {

    }

    @Test
    void removePhotoAbsent() {

    }
}
