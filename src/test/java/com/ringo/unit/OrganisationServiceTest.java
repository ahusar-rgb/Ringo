package com.ringo.unit;

import com.ringo.auth.AuthenticationService;
import com.ringo.dto.company.OrganisationRequestDto;
import com.ringo.dto.company.OrganisationResponseDto;
import com.ringo.exception.NotFoundException;
import com.ringo.exception.UserException;
import com.ringo.mapper.company.LabelMapperImpl;
import com.ringo.mapper.company.OrganisationMapper;
import com.ringo.mapper.company.OrganisationMapperImpl;
import com.ringo.mock.common.MultipartFileMock;
import com.ringo.mock.dto.OrganisationDtoMock;
import com.ringo.mock.model.OrganisationMock;
import com.ringo.model.company.Organisation;
import com.ringo.model.photo.Photo;
import com.ringo.repository.OrganisationRepository;
import com.ringo.repository.UserRepository;
import com.ringo.service.common.PhotoService;
import com.ringo.service.company.OrganisationService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrganisationServiceTest {
    @Mock
    private OrganisationRepository organisationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private PhotoService photoService;

    @Captor
    private ArgumentCaptor<Organisation> organisationCaptor;

    @Spy
    private OrganisationMapper mapper = new OrganisationMapperImpl();
    @InjectMocks
    private OrganisationService service;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(service, "passwordEncoder", new BCryptPasswordEncoder());
        ReflectionTestUtils.setField(service, "mapper", mapper);
        ReflectionTestUtils.setField(mapper, "labelMapper", new LabelMapperImpl());
        ReflectionTestUtils.setField(service, "abstractUserMapper", mapper);
        ReflectionTestUtils.setField(service, "repository", organisationRepository);
    }

    @Test
    void findByIdSuccess() {
        // given
        Long id = 1L;
        Organisation expected = OrganisationMock.getOrganisationMock();
        expected.setId(id);
        // when
        when(organisationRepository.findByIdActiveWithEvents(id)).thenReturn(Optional.of(expected));
        // then
        OrganisationResponseDto responseDto = service.findById(id);
        // assert
        assertThat(responseDto).isNotNull();
        OrganisationResponseDto expectedDto = mapper.toDto(expected);

        assertThat(responseDto).isEqualTo(expectedDto);
        verify(organisationRepository, times(1)).findByIdActiveWithEvents(id);
    }

    @Test
    void findByIdNotFound() {
        // given
        Long id = 1L;
        // when
        when(organisationRepository.findByIdActiveWithEvents(id)).thenReturn(Optional.empty());
        // then
        assertThrows(NotFoundException.class, () -> service.findById(id));
    }

    @Test
    void findCurrentParticipant() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));

        //then
        OrganisationResponseDto responseDto = service.findCurrentOrganisation();
        assertThat(responseDto).isNotNull();
        OrganisationResponseDto expectedDto = mapper.toDto(organisation);
        assertThat(responseDto).isEqualTo(expectedDto);
    }

    @Test
    void saveSuccess() {
        // given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setIsActive(false);
        OrganisationRequestDto dto = OrganisationDtoMock.getOrganisationMockDto();
        dto.setEmail(organisation.getEmail());
        dto.setUsername(organisation.getUsername());
        final String emailVerificationToken = "token";
        // when
        when(organisationRepository.save(organisationCaptor.capture())).thenReturn(organisation);
        when(userRepository.findVerifiedByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findActiveByUsername(dto.getUsername())).thenReturn(Optional.empty());
        // then
        OrganisationResponseDto responseDto = service.save(dto);
        // assert
        Organisation saved = organisationCaptor.getAllValues().get(0);
        assertThat(saved).isNotNull();
        assertThat(saved).usingRecursiveComparison().ignoringFields("createdAt", "id", "password").isEqualTo(organisation);
        assertThat(saved.getPassword()).isNotNull();
        assertThat(saved.getPassword()).isNotEqualTo(organisation.getPassword());
        assertThat(saved.getCreatedAt().getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());
        assertThat(saved.getCreatedAt().getYear()).isEqualTo(LocalDate.now().getYear());
        assertThat(saved.getUpdatedAt()).isNull();

        assertThat(responseDto).isNotNull();
        OrganisationResponseDto expectedDto = mapper.toDto(organisation);

        assertThat(responseDto).isEqualTo(expectedDto);
        verify(organisationRepository, times(1)).save(saved);

        verify(authenticationService, times(1)).sendVerificationEmail(organisationCaptor.capture());
        assertThat(organisationCaptor.getAllValues().get(1).getEmail()).isEqualTo(saved.getEmail());
    }

    @Test
    void partialUpdateSuccess() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        OrganisationRequestDto dto = OrganisationDtoMock.getOrganisationMockDto();
        dto.setEmail(organisation.getEmail());
        //when
        when(userRepository.findActiveByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findVerifiedByEmail(organisation.getEmail())).thenReturn(Optional.empty());
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));
        when(organisationRepository.save(organisationCaptor.capture())).thenReturn(organisation);
        //then
        OrganisationResponseDto responseDto = service.partialUpdate(dto);

        Organisation saved = organisationCaptor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved).usingRecursiveComparison().ignoringFields("updatedAt").isEqualTo(organisation);
        assertThat(saved.getUpdatedAt().getDayOfYear()).isEqualTo(LocalDate.now().getDayOfYear());
        assertThat(saved.getUpdatedAt().getYear()).isEqualTo(LocalDate.now().getYear());

        assertThat(responseDto).isNotNull();
        OrganisationResponseDto expectedDto = mapper.toDto(organisation);

        assertThat(responseDto).isEqualTo(expectedDto);

        verify(organisationRepository, times(1)).save(any(Organisation.class));
    }

    @Test
    void getFullUserSuccess() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));

        //then
        Organisation returned = service.getFullUser();
        assertThat(returned).isEqualTo(organisation);
    }

    @Test
    void getFullUserNotFound() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.empty());

        //then
        assertThrows(NotFoundException.class, () -> service.getFullUser());
    }

    @Test
    void throwIfRequiredFieldsNotFilledNoUsername() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setUsername(null);

        //then
        assertThrows(UserException.class, () -> service.throwIfRequiredFieldsNotFilled(organisation));
    }

    @Test
    void throwIfRequiredFieldsNotFilledNoName() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setName(null);

        //then
        assertThrows(UserException.class, () -> service.throwIfRequiredFieldsNotFilled(organisation));
    }

    @Test
    void throwIfUniqueConstraintsViolatedEmailTaken() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(System.currentTimeMillis() - 100);
        Organisation found = OrganisationMock.getOrganisationMock();
        found.setId(System.currentTimeMillis());
        //when
        when(userRepository.findVerifiedByEmail(organisation.getEmail())).thenReturn(Optional.of(found));
        when(userRepository.findActiveByUsername(organisation.getUsername())).thenReturn(Optional.empty());
        //then
        assertThrows(UserException.class, () -> service.throwIfUniqueConstraintsViolated(organisation));
    }

    @Test
    void throwIfUniqueConstraintsViolatedUsernameTaken() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setId(System.currentTimeMillis() - 100);
        Organisation found = OrganisationMock.getOrganisationMock();
        found.setId(System.currentTimeMillis());
        //when
        when(userRepository.findActiveByUsername(organisation.getUsername())).thenReturn(Optional.of(found));
        //then
        assertThrows(UserException.class, () -> service.throwIfUniqueConstraintsViolated(organisation));
    }

    @Test
    void deleteSuccess() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));
        //then
        service.delete();
        verify(organisationRepository, times(1)).delete(any(Organisation.class));
    }

    @Test
    void activateSuccess() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setIsActive(false);
        organisation.setEmailVerified(true);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.save(organisationCaptor.capture())).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));
        //then
        OrganisationResponseDto activated = service.activate();
        assertThat(activated).isNotNull();
        assertThat(activated.getIsActive()).isTrue();
        assertThat(activated).usingRecursiveComparison().ignoringFields("isActive").isEqualTo(mapper.toDto(organisation));
        verify(organisationRepository, times(1)).save(any(Organisation.class));
    }

    @Test
    void activateAlreadyActive() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setIsActive(true);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));
        //then
        assertThrows(UserException.class, () -> service.activate());
    }

    @Test
    void activateEmailNotVerified() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setIsActive(false);
        organisation.setEmailVerified(false);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));
        //then
        assertThrows(UserException.class, () -> service.activate());
    }

    @Test
    void activateRequiredFieldsNotFilled() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setIsActive(false);
        organisation.setName(null);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));
        //then
        assertThrows(UserException.class, () -> service.activate());
    }

    @Test
    void setPhotoSuccess() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        MultipartFile file = MultipartFileMock.getMockMultipartFile();

        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));
        when(organisationRepository.save(organisation)).thenReturn(organisation);

        //then
        OrganisationResponseDto dto = service.setPhoto(file);
        assertThat(dto).isEqualTo(mapper.toDto(organisation));

        verify(organisationRepository, times(1)).save(any(Organisation.class));

        try {
            verify(photoService, times(1)).save("profilePictures/user#" + organisation.getId(), "png", file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void removePhotoSuccess() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        Photo photo = new Photo();
        photo.setId(System.currentTimeMillis());
        organisation.setProfilePicture(photo);

        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));
        when(organisationRepository.save(organisation)).thenReturn(organisation);

        //then
        OrganisationResponseDto dto = service.removePhoto();
        assertThat(dto).isEqualTo(mapper.toDto(organisation));

        verify(organisationRepository, times(1)).save(any(Organisation.class));
        verify(photoService, times(1)).delete(photo.getId());
    }

    @Test
    void removePhotoAbsent() {
        //given
        Organisation organisation = OrganisationMock.getOrganisationMock();
        organisation.setProfilePicture(null);
        //when
        when(authenticationService.getCurrentUser()).thenReturn(organisation);
        when(organisationRepository.findFullById(organisation.getId())).thenReturn(Optional.of(organisation));

        //then
        assertThrows(UserException.class, () -> service.removePhoto());

        verify(photoService, never()).delete(anyLong());
    }
}
