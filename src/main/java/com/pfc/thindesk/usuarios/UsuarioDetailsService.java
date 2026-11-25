package com.pfc.thindesk.usuarios;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pfc.thindesk.usuarios.exception.UsuarioNaoEncontradoException;

import lombok.RequiredArgsConstructor;

/**
 * Service para carregamento dos usuários para Spring Security
 * 
 * @author
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

        private final UsuarioService usuarioService;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                Usuario usuario = usuarioService.encontrarPorUsername(username)
                                .orElseThrow(UsuarioNaoEncontradoException::new);
                return usuario;
        }
}
