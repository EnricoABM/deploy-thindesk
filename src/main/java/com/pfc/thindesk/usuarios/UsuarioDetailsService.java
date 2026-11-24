package com.pfc.thindesk.usuarios;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Service para consulta de usuários
 * 
 * @author Enrico Bertozzi
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioService usuarioService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioService.encontrarPorUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        List<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getPerfil().name()));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getSenha())
                .authorities(authorities)
                .disabled(!usuario.isAtivo())
                .build();
    }
}
