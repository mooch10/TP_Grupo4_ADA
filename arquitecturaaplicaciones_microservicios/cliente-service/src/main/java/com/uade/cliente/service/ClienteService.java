package com.uade.cliente.service;

import com.uade.cliente.entity.Cliente;
import com.uade.cliente.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cliente findById(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    public Cliente create(Cliente cliente) {
        cliente.setId(null);
        return clienteRepository.save(cliente);
    }

    public Cliente update(Long id, Cliente cliente) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        existente.setNombre(cliente.getNombre());
        existente.setTelefono(cliente.getTelefono());
        return clienteRepository.save(existente);
    }

    public void delete(Long id) {
        Cliente existente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        clienteRepository.delete(existente);
    }
}
