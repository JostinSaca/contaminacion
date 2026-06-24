package com.example.GUI.views;


import com.mongodb.client.MongoClients;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import com.example.Modelos.Zona;
import com.example.Modelos.ResultadoPrediccion;
import java.util.List;

@Service
public class MongoService {

    private final MongoTemplate mongoTemplate;

    public MongoService() {
        String connectionString = "mongodb+srv://jostinsaca53_db_user:jostinmongo@cluster0.7sqy2n9.mongodb.net/sistema_ambiental_db?retryWrites=true&w=majority&appName=Cluster0";
        this.mongoTemplate = new MongoTemplate(MongoClients.create(connectionString), "sistema_ambiental_db");
    }

    // Guarda cada zona individualmente para evitar el error de colección
    public void guardarZonas(Zona[] zonas) {
        mongoTemplate.dropCollection("zonas");
        for (Zona z : zonas) {
            mongoTemplate.save(z, "zonas");
        }
    }

    // Lee la lista de zonas desde la base de datos y la convierte a un arreglo Zona[]
    public Zona[] leerZonas() {
        List<Zona> lista = mongoTemplate.findAll(Zona.class, "zonas");
        if (lista.isEmpty()) return null;
        return lista.toArray(new Zona[0]);
    }

    // Guarda una nueva predicción de forma acumulativa
    public void guardarPrediccion(ResultadoPrediccion r) {
        mongoTemplate.save(r, "predicciones");
    }

    // Lee todas las predicciones acumuladas
    public List<ResultadoPrediccion> leerPredicciones() {
        return mongoTemplate.findAll(ResultadoPrediccion.class, "predicciones");
    }
}