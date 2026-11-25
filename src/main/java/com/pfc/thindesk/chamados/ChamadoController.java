package com.pfc.thindesk.chamados;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pfc.thindesk.chamados.dto.ChamadoRequest;
import com.pfc.thindesk.chamados.dto.ChamadoResponse;
import com.pfc.thindesk.usuarios.Usuario;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public ChamadoResponse criarChamado(@RequestBody ChamadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        return ChamadoResponse.de(chamadoService.criarChamado(request.paraEntidade(), usuario));
    }

    @GetMapping
    public List<ChamadoResponse> listarChamados(@AuthenticationPrincipal Usuario usuario) {
        return chamadoService.listarVisiveis(usuario).stream().map(ChamadoResponse::de).toList();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TECNICO','ADMINISTRADOR')")
    public ResponseEntity<ChamadoResponse> atualizarChamado(@PathVariable String id,
            @RequestBody ChamadoRequest request,
            @AuthenticationPrincipal Usuario usuario) {
        Chamado atualizado = chamadoService.atualizarChamado(id, request.paraEntidade(), usuario);
        return ResponseEntity.ok(ChamadoResponse.de(atualizado));
    }
}
