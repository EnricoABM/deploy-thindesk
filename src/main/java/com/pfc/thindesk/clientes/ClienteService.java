package com.pfc.thindesk.clientes;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.pfc.thindesk.clientes.exception.ClienteInvalidoException;
import com.pfc.thindesk.clientes.exception.ClienteNaoEncontradoException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private static final int NOME_MINIMO = 3;
    private static final int NOME_MAXIMO = 100;
    private static final int TELEFONE_MINIMO = 10;
    private static final int TELEFONE_MAXIMO = 11;

    private final ClienteRepository clienteRepository;

    /**
     * Valida e salva um novo cliente.
     * 
     * @param cliente dados do cliente
     * 
     * @return cliente salvo
     */
    public Cliente novoCliente(Cliente cliente) {
        validarCliente(cliente);
        normalizar(cliente);
        return clienteRepository.save(cliente);
    }

    /**
     * Lista clientes por parte do nome e por setor exato, sem diferenciar
     * maiúsculas.
     * 
     * @param nome  trecho do nome, nulo ou vazio não filtra
     * @param setor setor escolhido, nulo ou vazio não filtra
     * 
     * @return clientes ordenados por nome
     */
    public List<Cliente> filtrar(String nome, String setor) {
        String termo = nome == null ? "" : nome.trim().toLowerCase(Locale.ROOT);
        String setorEscolhido = setor == null ? "" : setor.trim();
        return clienteRepository.findAll().stream()
                .filter(c -> termo.isEmpty() || contem(c.getNome(), termo))
                .filter(c -> setorEscolhido.isEmpty() || setorEscolhido.equalsIgnoreCase(c.getSetor()))
                .sorted(Comparator.comparing(c -> c.getNome() == null ? "" : c.getNome().toLowerCase(Locale.ROOT)))
                .toList();
    }

    /**
     * Lista os setores já usados nos clientes, sem repetição e em ordem alfabética.
     * 
     * @return setores cadastrados
     */
    public List<String> listarSetores() {
        return clienteRepository.findAll().stream()
                .map(Cliente::getSetor)
                .filter(s -> s != null && !s.isBlank())
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * Busca um cliente pelo id.
     * 
     * @param id identificador do cliente
     * @return cliente encontrado
     */
    public Cliente buscarCliente(String id) {
        return clienteRepository.findById(id).orElseThrow(ClienteNaoEncontradoException::new);
    }
     
    /** 
     * Valida e atualiza um cliente existente. 
     * 
     * @param id identificador do cliente 
     * @param dados novos valores 
     * 
     * @return cliente atualizado 
     */
    public Cliente atualizarCliente(String id, Cliente dados) {
        Cliente cliente = buscarCliente(id);
        validarCliente(dados);
        normalizar(dados);
        cliente.setNome(dados.getNome());
        cliente.setTelefone(dados.getTelefone());
        cliente.setSetor(dados.getSetor());
        return clienteRepository.save(cliente);
    }

    /** 
     * Remove um cliente existente. 
     * 
     * @param id identificador do cliente 
     */
    public void excluirCliente(String id) {
        buscarCliente(id);
        clienteRepository.deleteById(id);
    }
     
    /** 
     * Remove espaços nas bordas de nome, telefone e setor. 
     * 
     * @param cliente cliente a ajustar 
     */
    private void normalizar(Cliente cliente) {
        cliente.setNome(cliente.getNome().trim());
        cliente.setTelefone(cliente.getTelefone().trim());
        cliente.setSetor(cliente.getSetor().trim());
    }
     
    /** 
     * Verifica se o valor contém o termo, sem diferenciar maiúsculas. 
     * 
     * @param valor texto do campo 
     * @param termo termo em minúsculas 
     * 
     * @return verdadeiro se contém 
     */
    private static boolean contem(String valor, String termo) {
        return valor != null && valor.toLowerCase(Locale.ROOT).contains(termo);
    }

    /**
     * Confere os campos obrigatórios e os limites de nome e telefone.
     * 
     * @param cliente dados a validar
     * 
     */
    private void validarCliente(Cliente cliente) {
        String nome = cliente.getNome() == null ? "" : cliente.getNome().trim();
        if (nome.isEmpty()) {
            throw new ClienteInvalidoException("Nome é obrigatório");
        }
        if (nome.length() < NOME_MINIMO || nome.length() > NOME_MAXIMO) {
            throw new ClienteInvalidoException(
                    "Nome deve ter de " + NOME_MINIMO + " a " + NOME_MAXIMO + " caracteres");
        }

        String telefone = cliente.getTelefone() == null ? "" : cliente.getTelefone().trim();
        if (telefone.isEmpty()) {
            throw new ClienteInvalidoException("Telefone é obrigatório");
        }
        int digitos = telefone.replaceAll("\\D", "").length();
        if (digitos < TELEFONE_MINIMO || digitos > TELEFONE_MAXIMO) {
            throw new ClienteInvalidoException("Telefone deve ter 10 ou 11 dígitos, com DDD");
        }

        String setor = cliente.getSetor() == null ? "" : cliente.getSetor().trim();
        if (setor.isEmpty()) {
            throw new ClienteInvalidoException("Setor é obrigatório");
        }
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }
}
