package com.pfc.thindesk.usuarios;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pfc.thindesk.usuarios.exception.UsuarioNaoExcluivelException;

import lombok.RequiredArgsConstructor;

/**
 * API pública do módulo usuarios. Outros módulos devem usar apenas esta classe.
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    static final int SENHA_MINIMA = 8;
    static final int EMAIL_MAXIMO = 254;
    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public Usuario criarUsuario(Usuario usuario) {

        if (usuario.getPerfil() == null) {
            throw new IllegalArgumentException("Perfil do usuário é obrigatório");
        }
        if (usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            throw new IllegalArgumentException("Senha do usuário é obrigatória");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setAtivo(true);
        return usuarioRepository.save(usuario);
    }

    /** Cadastro feito pelo administrador (atendente ou técnico). */
    public Usuario cadastrarOperador(Usuario usuario) {
        String email = normalizarEmail(usuario.getUsername());
        validarOperador(email, usuario.getNome(), usuario.getPerfil());
        validarSenha(usuario.getPassword());

        if (encontrarPorUsername(email).isPresent()) {
            throw new IllegalStateException("Já existe um usuário com o email '" + email + "'");
        }

        usuario.setUsername(email);
        usuario.setNome(usuario.getNome().trim());
        return criarUsuario(usuario);
    }

    /**
     * Busca um operador (atendente ou técnico) para edição. Administradores não são
     * editáveis
     * por esta funcionalidade.
     */
    public Usuario buscarOperador(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR) {
            throw new IllegalArgumentException("Administrador não pode ser editado por esta tela");
        }
        return usuario;
    }

    /**
     * Atualiza um operador. Senha em branco mantém a senha atual; senha informada é
     * validada e
     * gravada como hash.
     */
    public Usuario atualizarOperador(String id, String username, String nome, Perfil perfil,
            boolean ativo, String senhaNova) {
        Usuario usuario = buscarOperador(id);
        String email = normalizarEmail(username);
        validarOperador(email, nome, perfil);

        if (!email.equals(usuario.getUsername())) {
            if (encontrarPorUsername(email).isPresent()) {
                throw new IllegalStateException("Já existe um usuário com o email '" + email + "'");
            }
            usuario.setUsername(email);
        }
        usuario.setNome(nome.trim());
        usuario.setPerfil(perfil);
        usuario.setAtivo(ativo);

        if (senhaNova != null && !senhaNova.isBlank()) {
            validarSenha(senhaNova);
            usuario.setPassword(passwordEncoder.encode(senhaNova));
        }
        return usuarioRepository.save(usuario);
    }

    /**
     * Remove um usuário. Não permite excluir a própria conta nem o último administrador.
     * 
     * @param id identificador do usuário a remover
     * @param idSolicitante identificador do administrador logado
     */
    public void excluirUsuario(String id, String idSolicitante) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado"));
        if (usuario.getId().equals(idSolicitante)) {
            throw new UsuarioNaoExcluivelException("Você não pode excluir a própria conta");
        }
        if (usuario.getPerfil() == Perfil.ADMINISTRADOR
                && listarPorPerfil(Perfil.ADMINISTRADOR).size() <= 1) {
            throw new UsuarioNaoExcluivelException("Não é possível excluir o último administrador");
        }
        usuarioRepository.delete(usuario);
    }

    /**
     * Lista usuários por texto (nome ou login, sem diferenciar maiúsculas) e
     * perfil. Nulos ou vazios não filtram.
     */
    public List<Usuario> filtrar(String texto, Perfil perfil) {
        String termo = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return usuarioRepository.findAll().stream()
                .filter(u -> perfil == null || u.getPerfil() == perfil)
                .filter(u -> termo.isEmpty() || contem(u.getNome(), termo) || contem(u.getUsername(), termo))
                .sorted(Comparator.comparing(u -> u.getNome() == null ? "" : u.getNome().toLowerCase(Locale.ROOT)))
                .toList();
    }

    public Optional<Usuario> encontrarPorUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarPorPerfil(Perfil perfil) {
        return usuarioRepository.findByPerfil(perfil);
    }

    /** Email em minúsculas e sem espaços nas bordas; nulo permanece nulo para ser rejeitado depois. */
    private static String normalizarEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private void validarOperador(String email, String nome, Perfil perfil) {
        if (perfil != Perfil.ATENDENTE && perfil != Perfil.TECNICO) {
            throw new IllegalArgumentException("Só é possível cadastrar atendente ou técnico");
        }
        if (email == null || email.length() > EMAIL_MAXIMO || !EMAIL_VALIDO.matcher(email).matches()) {
            throw new IllegalArgumentException("Usuário deve ser um email válido (ex.: nome@dominio.com)");
        }
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < SENHA_MINIMA) {
            throw new IllegalArgumentException("Senha deve ter ao menos " + SENHA_MINIMA + " caracteres");
        }
    }

    private static boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }
}
