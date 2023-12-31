package az.atl.msauth.service.impl;

import az.atl.msauth.dao.entity.UserCredentialsEntity;
import az.atl.msauth.dao.entity.UserInfoEntity;
import az.atl.msauth.dao.repository.UserCredentialsRepository;
import az.atl.msauth.dto.request.profile.AgentProfileRequest;
import az.atl.msauth.dto.request.profile.SuperVisorProfileRequest;
import az.atl.msauth.dto.request.profile.UpdateAccountRequest;
import az.atl.msauth.dto.request.profile.UpdatePasswordRequest;
import az.atl.msauth.dto.response.message.DeleteResponse;
import az.atl.msauth.dto.response.message.UpdateResponse;
import az.atl.msauth.exceptions.*;
import az.atl.msauth.feign.MessageFeignClient;
import az.atl.msauth.service.AgentProfileService;
import az.atl.msauth.service.security.AuthenticationService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Null;
import org.springframework.cglib.core.Local;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

@Service
public class AgentProfileServiceImpl implements AgentProfileService {
    private final UserCredentialsRepository repository;
    @PersistenceContext
    private final EntityManager manager;
    private final MessageFeignClient feignClient;
    private final SuperVisorProfileServiceImpl superVisorProfileService;
    private final AuthenticationService authenticationService;
    private final MessageSource messageSource;

    private final BCryptPasswordEncoder encoder;

    public AgentProfileServiceImpl(UserCredentialsRepository repository, EntityManager manager, MessageFeignClient feignClient, SuperVisorProfileServiceImpl superVisorProfileService, AuthenticationService authenticationService, MessageSource messageSource, BCryptPasswordEncoder encoder) {
        this.repository = repository;
        this.manager = manager;
        this.feignClient = feignClient;
        this.superVisorProfileService = superVisorProfileService;
        this.authenticationService = authenticationService;
        this.messageSource = messageSource;
        this.encoder = encoder;
    }

    @Transactional
    @Override
    public AgentProfileRequest myProfile() {
        Authentication contextHolder = SecurityContextHolder.getContext().getAuthentication();
        UserCredentialsEntity entity = repository.findByUsername(contextHolder.getName()).get();
        SecurityContextHolder.clearContext();

        return AgentProfileRequest.builder()
                .name(entity.getUserInfoEntity().getName())
                .surname(entity.getUserInfoEntity().getSurname())
                .birthDate(entity.getUserInfoEntity().getBirthDate())
                .email(entity.getUserInfoEntity().getEmail())
                .phoneNumber(entity.getUserInfoEntity().getPhoneNumber())
                .createdAt(entity.getCreatedAt())
                .username(entity.getUsername())
                .build();
    }

    @Transactional
    @Override
    public DeleteResponse deleteMyAccount(String header, String lang) {
        Authentication contextHolder = SecurityContextHolder.getContext().getAuthentication();
        UserCredentialsEntity entity = repository.findByUsername(contextHolder.getName()).get();
        repository.delete(entity);

        feignClient.deleteUser(header, lang);

        authenticationService.revokeAllUserTokens(entity.getUserInfoEntity());
        SecurityContextHolder.clearContext();
        return DeleteResponse.builder()
                .isDeleted(true).build();
    }

    @Transactional
    @Override
    public UpdateResponse updateMyAccount(String header, String lang, UpdateAccountRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        UserCredentialsEntity userCredentials = repository.findByUsername(authentication.getName())
                .orElseThrow(() -> new UserNotFoundException(messageSource.getMessage("user_not_found", null, LocaleContextHolder.getLocale())));

        if (Objects.isNull(request)) throw new NullPointerException(new NullPointerException().getLocalizedMessage());

        UserInfoEntity entity = manager.find(UserInfoEntity.class, userCredentials.getUserInfoEntity().getId());

        List<SuperVisorProfileRequest> list = superVisorProfileService.getAll();

        List<SuperVisorProfileRequest> updateList = list.stream()
                .filter(o -> o.getEmail().equals(request.getEmail()) || o.getUsername().equals(request.getUsername()) || o.getPhoneNumber().equals(request.getPhoneNumber()))
                .filter(o -> !o.getUsername().equals(entity.getUserCredentials().getUsername()) || !o.getEmail().equals(entity.getEmail()) || !o.getPhoneNumber().equals(entity.getPhoneNumber()))
                .toList();

        System.out.println(updateList);
        if (!updateList.isEmpty()) {
            throw new UniqueConstraintException(messageSource.getMessage("unique_constraint", null, LocaleContextHolder.getLocale()));
        }
        if(!entity.getEmail().equals(request.getEmail())) {
            entity.setEmail(request.getEmail());
        }
        if(!entity.getPhoneNumber().equals(request.getPhoneNumber())) {
            entity.setPhoneNumber(request.getPhoneNumber());
        }
        entity.setName(request.getName());
        entity.setBirthDate(request.getBirthDate());
        entity.setPhoneNumber(request.getPhoneNumber());
        entity.setSurname(request.getSurname());
        entity.getUserCredentials().setUsername(request.getUsername());

        if (!request.getUsername().equals(authentication.getName())) {
            feignClient.updateUsername(header, lang, request.getUsername());
            authenticationService.revokeAllUserTokens(entity);
        }
        authenticationService.revokeAllUserTokens(entity);
        SecurityContextHolder.clearContext();
        return UpdateResponse.builder()
                .isUpdated(true).build();
    }

    @Transactional
    @Override
    public UpdateResponse updatePassword(UpdatePasswordRequest request) {
        Authentication contextHolder = SecurityContextHolder.getContext().getAuthentication();
        UserCredentialsEntity entity = repository.findByUsername(contextHolder.getName()).get();
        if (encoder.matches(request.getCurrentPassword(), entity.getHash())) {
            if (Objects.equals(request.getNewPassword(), request.getRepeatNewPassword())) {
                if (Objects.equals(request.getCurrentPassword(), request.getNewPassword())) {
                    throw new IdenticalPasswordsException(messageSource.getMessage("identical_passwords", null, LocaleContextHolder.getLocale()));
                }
                entity.setHash(encoder.encode(request.getNewPassword()));
                authenticationService.revokeAllUserTokens(entity.getUserInfoEntity());
                SecurityContextHolder.clearContext();
                return UpdateResponse.builder()
                        .isUpdated(true).build();
            }
            throw new IncorrectPasswordException(messageSource.getMessage("incorrect_password", null, LocaleContextHolder.getLocale()));
        }
        throw new IncorrectPasswordException(messageSource.getMessage("incorrect_password", null, LocaleContextHolder.getLocale()));
    }
}
