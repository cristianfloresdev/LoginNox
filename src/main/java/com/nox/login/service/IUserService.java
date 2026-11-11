package com.nox.login.service;

import com.nox.login.entity.User;

import java.util.List;

/**
 * Interfaz que define la gestion de la entidad user
 * Define las funciones basics del proyecto CRUD sin importar la implementation
 */
public interface IUserService
{
    /**
     * Guarda un nuevo user o actualiza uno existente(si ya tiene ID)
     * @param user objeto user a guardar o actualizar
     * @return User guardado/actualizado
     */
    User guardarUser(User user);
}
