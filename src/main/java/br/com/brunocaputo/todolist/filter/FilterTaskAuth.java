package br.com.brunocaputo.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import at.favre.lib.crypto.bcrypt.BCrypt;
import br.com.brunocaputo.todolist.user.IUserRepository;
import br.com.brunocaputo.todolist.user.UserModel;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

  @Autowired
  private IUserRepository userRepository;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String authorization = request.getHeader("Authorization").substring("Basic".length()).trim();
    byte[] decodedAuthorization = Base64.getDecoder().decode(authorization);
    String[] credentials = (new String(decodedAuthorization)).split(":");
    String username = credentials[0];
    String password = credentials[1];

    UserModel user = this.userRepository.findByUsername(username);
    if (user == null) {
      response.sendError(HttpStatus.UNAUTHORIZED.value());
    } else {
      var verifyPassword = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

      filterChain.doFilter(request, response);
    }

  }
}
