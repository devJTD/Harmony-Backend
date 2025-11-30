package com.harmony.sistema.controller.publico;

import com.harmony.sistema.model.Noticia;
import com.harmony.sistema.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/blog") // Ruta que consumirá Angular
public class BlogRestController {

    private final BlogService blogService;

    @Autowired
    public BlogRestController(BlogService blogService) {
        this.blogService = blogService;
    }

    /**
     * Endpoint para obtener todas las noticias.
     * GET /api/blog
     */
    @GetMapping
    public List<Noticia> getAllNoticias() {
        return blogService.getAllNoticias();
    }

    /**
     * Endpoint para obtener una noticia por ID.
     * GET /api/blog/{id}
     */
    @GetMapping("/{id}")
    public Noticia getNoticiaById(@PathVariable Long id) {
        return blogService.getNoticiaById(id);
    }

    /**
     * Endpoint para agregar una nueva noticia.
     * POST /api/blog
     */
    @PostMapping
    public Noticia createNoticia(@RequestBody Noticia noticia) {
        return blogService.saveNoticia(noticia);
    }

}
