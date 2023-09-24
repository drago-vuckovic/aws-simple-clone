package co.vuckovic.pegasus.service;

import co.vuckovic.pegasus.model.dto.JwtUser;
import co.vuckovic.pegasus.repository.UserEntityRepository;
import co.vuckovic.pegasus.model.exception.EmailNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {

  private final UserEntityRepository userEntityRepository;
  private final ModelMapper modelMapper;

  @Override
  public JwtUser loadUserByUsername(String email) throws UsernameNotFoundException {
    return modelMapper.map(
        userEntityRepository.findByEmail(email).orElseThrow(EmailNotFoundException::new),
        JwtUser.class);
  }
}
