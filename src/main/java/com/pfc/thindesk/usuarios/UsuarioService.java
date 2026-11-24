package com.pfc.thindesk.usuarios;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario criarUsuario(String username, String nome, Perfil perfil, String senhaPura) {
        if (perfil == null) {
            throw new IllegalArgumentException("Perfil do usuário é obrigatório");
        }
        if (senhaPura == null || senhaPura.isBlank()) {
            throw new IllegalArgumentException("Senha do usuário é obrigatória");
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setNome(nome);
        usuario.setPerfil(perfil);
        usuario.setSenha(passwordEncoder.encode(senhaPura));
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> encontrarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public List<Usuario> listarPorPerfil(Perfil perfil) {
        return usuarioRepository.findByPerfil(perfil);
    }
}
