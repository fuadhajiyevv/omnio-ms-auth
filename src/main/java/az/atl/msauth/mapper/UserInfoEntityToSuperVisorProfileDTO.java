package az.atl.msauth.mapper;

import az.atl.msauth.dao.entity.UserInfoEntity;
import az.atl.msauth.dto.request.profile.SuperVisorProfileRequest;

public class UserInfoEntityToSuperVisorProfileDTO {

    public static SuperVisorProfileRequest mapToSuperVisorProfileDTO(UserInfoEntity entity) {
        return SuperVisorProfileRequest.builder()
                .id(entity.getId())
                .name(entity.getName())
                .surname(entity.getSurname())
                .birthDate(entity.getBirthDate())
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .username(entity.getUserCredentials().getUsername())
                .password(entity.getUserCredentials().getPassword())
                .role(entity.getUserCredentials().getRole().getRole())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
