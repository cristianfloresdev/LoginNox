package com.nox.login.service;

import com.nox.login.entity.Password;
import com.nox.login.entity.User;
import com.nox.login.excepciones.PasswordInvalidoException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Aquí vamos a desarrollar la logica del password, el cual le vamos a implementar validaciones de seguridad
 * y logica abstracta pero eficiente
 */
@Service
public class PasswordService
{
    @Autowired
    private BCryptPasswordEncoder encoder;

    // =======================================
    //     Métodos de validaciones Password
    // =======================================
    /**
     * Aqui vamos a crear la password cifrada
     * @param user
     * @param plainPassword
     * @return
     */
    public Password crearPasswordParaUsuario(User user, String plainPassword)
    {
        validarFortalezaPassword(plainPassword);
        Password password = new Password();
        password.setUser(user);
        password.setHash(encoder.encode(plainPassword)); //Cifrado
        password.setDateCreation(LocalDate.now());
        password.setExpirada(false);
        return password;
    }

    /**
     * Verificamos si la password es correcta
     * @param passwordPlano
     * @param hashGuardado
     * @return
     */
    public boolean verificarPassword(String passwordPlano, String hashGuardado)
    {
        return encoder.matches(passwordPlano, hashGuardado);
    }

    /**
     * Válida la fortaleza del password antes de ser guardada
     *
     * Regla aplicada:
     * Debe tener minimo 8 caracteres
     * Debe incluir al menos 1 letra mayúscula
     * Debe incluir al menos 1 número
     * Debe incluir al menos 1 caracter especial
     * Si alguna regla no se cumple, lanza una PasswordInvalidoException
     */
    private void validarFortalezaPassword(String password)
    {
        //Validamos la longitud minima
        if(password.length() < 8)
        {
            throw new PasswordInvalidoException("La password debe tener al menos 8 caracteres");
        }

        //Validamos de que contenga al menos una letra mayuscula
        if(!password.matches(".*[A-Z].*"))
        {
            throw new PasswordInvalidoException("La password debe contener al menos una letra mayúscula");
        }

        //Validamos de que contenga al menos un numero
        if(!password.matches(".*\\d.*"))
        {
            throw new PasswordInvalidoException("La password debe contener al menos un numero");
        }

        //Validamos de que contenga al menos un caracter especial
        if(!password.matches(".*[!@#$%^&*()_+\\-={}:;\"',.<>?].*"))
        {
            throw new PasswordInvalidoException("La password debe contener al menos un caracter especial");
        }
    }
}
