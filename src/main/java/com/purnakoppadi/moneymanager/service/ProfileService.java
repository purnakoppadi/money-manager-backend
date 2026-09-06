package com.purnakoppadi.moneymanager.service;

import com.purnakoppadi.moneymanager.dto.AuthDto;
import com.purnakoppadi.moneymanager.dto.ProfileDto;
import com.purnakoppadi.moneymanager.entity.ProfileEntity;
import com.purnakoppadi.moneymanager.repository.ProfileRepository;
import com.purnakoppadi.moneymanager.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileService {
    private final PasswordEncoder passwordEncoder;
    private final ProfileRepository profileRepository;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Value("${app.activation.url}")
    private String activationUrl;

    public ProfileDto registerProfile(ProfileDto profileDTO) {

        ProfileEntity profileEntity = toEntity(profileDTO);
        profileEntity.setActivationToken(UUID.randomUUID().toString());
        profileEntity.setPassword(passwordEncoder.encode(profileEntity.getPassword()));


        profileEntity=profileRepository.save(profileEntity);

        String activationLink=activationUrl+"/api/v1.0/activate?token=" + profileEntity.getActivationToken();
        String subject="Activate your Money Manager account";
        String body="Clcik on the following link to activate your account: "+activationLink;
        emailService.sendEmail(profileEntity.getEmail(),subject,body);
        return  toDTO(profileEntity);

    }

    public ProfileEntity toEntity(ProfileDto profileDTO) {

        return ProfileEntity.builder()
                .id(profileDTO.getId())
                .fullName(profileDTO.getFullName())
                .email(profileDTO.getEmail())
                .password(profileDTO.getPassword())
                .profileImageUrl(profileDTO.getProfileImageUrl())
                .createAt(profileDTO.getCreateAt())
                .updateAt(profileDTO.getUpdateAt())
                .build();
    }

    public ProfileDto toDTO(ProfileEntity profileEntity) {
        return ProfileDto.builder()
                .id(profileEntity.getId())
                .fullName(profileEntity.getFullName())
                .email(profileEntity.getEmail())
                .profileImageUrl(profileEntity.getProfileImageUrl())
                .createAt(profileEntity.getCreateAt())
                .updateAt(profileEntity.getUpdateAt())
                .build();
    }
    public boolean activateProfile(String activationToken)
    {
        return profileRepository.findByActivationToken(activationToken)
                .map(profile->{
                    profile.setIsActive(true);
                    profileRepository.save(profile);
                    return true;
                })
                .orElse(false);

    }

    public boolean isAccountActive(String email)
    {
        return profileRepository.findByEmail(email)
                .map(ProfileEntity::getIsActive)
                .orElse(false);
    }

    public ProfileEntity getCurrentProfile() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        return profileRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Profile not found with email: " + email));
    }

    public ProfileDto getPublicProfile(String email)
    {
        ProfileEntity currentUser=null;
        if(email==null)
        {
           currentUser= getCurrentProfile();
        }else{
            currentUser=profileRepository.findByEmail(email)
                    .orElseThrow(()->new UsernameNotFoundException("Profile not found with email:"+email));
        }
        return ProfileDto.builder()
                .id(currentUser.getId())
                .fullName(currentUser.getFullName())
                .email(currentUser.getEmail())
                .profileImageUrl(currentUser.getProfileImageUrl())
                .createAt(currentUser.getCreateAt())
                .updateAt(currentUser.getUpdateAt())
                .build();
    }

    public Map<String,Object> authenticateAndGenerateToken(AuthDto authDto) {
        try{
          authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDto.getEmail(),authDto.getPassword()));
          String token=jwtUtil.generateToken(authDto.getEmail());
          return Map.of(
                  "token",token,
                  "user",getPublicProfile(authDto.getEmail())
          );
        } catch (Exception e) {
            throw new RuntimeException("Invalid email or password");
        }
    }
}