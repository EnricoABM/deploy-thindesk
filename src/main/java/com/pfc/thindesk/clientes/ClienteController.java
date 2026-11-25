package com.pfc.thindesk.clientes;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pfc.thindesk.clientes.dto.ClienteRequest;
import com.pfc.thindesk.clientes.dto.ClienteResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ATENDENTE','ADMINISTRADOR')")
    public ClienteResponse novoCliente(@RequestBody ClienteRequest request) {
        return ClienteResponse.de(clienteService.novoCliente(request.paraEntidade()));
    }

    @GetMapping
    public List<ClienteResponse> listarClientes() {
        return clienteService.listarClientes().stream().map(ClienteResponse::de).toList();
    }
}
