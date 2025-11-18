package com.harmony.sistema.service;

import com.harmony.sistema.model.Noticia;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BlogService {

    // Lista simulando base de datos
    private final List<Noticia> noticias = new ArrayList<>();

    public BlogService() {
        // Noticias de ejemplo
        noticias.add(new Noticia(1L,
                "¡Nuestro estudiante André Huarcaya triunfa en el Concurso de Piano de IBP!",
                "Estamos llenos de orgullo al anunciar que nuestro talentoso estudiante de piano, André Huarcaya, ha logrado el primer lugar en el prestigioso Concurso de Piano de la Escuela de Música IBP. André, bajo la guía de su profesor, se preparó con dedicación y demostró un dominio excepcional del instrumento. Su participación fue posible gracias al apoyo y patrocinio de la academia Harmony, que lo acompañó en cada etapa. ¡Felicidades, André! 🎉",
                "/noticia1.png"));

        noticias.add(new Noticia(2L,
                "Consejos para principiantes: Primeros pasos en el piano",
                "¿Acabas de empezar a tocar el piano? Te compartimos 5 consejos clave para que tu aprendizaje sea más fácil y divertido. Aprende sobre postura, técnica de digitación y cómo practicar de manera efectiva para ver resultados rápidamente.",
                "/blog1.jpg"));

        noticias.add(new Noticia(3L,
                "Los beneficios de aprender un instrumento musical desde joven",
                "Aprender a tocar un instrumento no solo desarrolla habilidades artísticas, sino también cognitivas y emocionales. En Harmony, creemos que la música es una herramienta poderosa para el crecimiento personal y académico de los estudiantes. Entre los principales beneficios se destacan: mejora la concentración y la memoria, fomenta la disciplina y la perseverancia, potencia la creatividad y el pensamiento crítico, promueve el trabajo en equipo y la empatía, y aumenta la confianza y la expresión emocional.",
                "/aprendizaje-musical.jpg"));
    }

    // Método para obtener todas las noticias
    public List<Noticia> getAllNoticias() {
        return noticias;
    }

    // Método para obtener noticia por ID
    public Noticia getNoticiaById(Long id) {
        return noticias.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // Método para agregar nueva noticia
    public Noticia saveNoticia(Noticia noticia) {
        noticias.add(noticia);
        return noticia;
    }

}
