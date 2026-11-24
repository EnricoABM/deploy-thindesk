package com.pfc.thindesk.usuarios;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Componente para inicialização do administrador inicial do sistema
 * 
 * @author Enrico Bertozzi
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInicialSeed implements ApplicationRunner {

    private final UsuarioService usuarioService;

    @Value("${thindesk.admin.usuario:admin}")
    private String username;

    @Value("${thindesk.admin.senha-inicial:}")
    private String senhaInicial;

    @Override
    public void run(ApplicationArguments args) {
        if (usuarioService.encontrarPorUsername(username).isPresent()) {
            return;
        }
        if (senhaInicial == null || senhaInicial.isBlank()) {
            log.warn("ADMIN_SENHA_INICIAL não definida: administrador '{}' não foi criado.", username);
            return;
        }
        usuarioService.criarUsuario(username, "Administrador", Perfil.ADMINISTRADOR, senhaInicial);
        log.info("Administrador inicial '{}' criado.", username);
    }
}
