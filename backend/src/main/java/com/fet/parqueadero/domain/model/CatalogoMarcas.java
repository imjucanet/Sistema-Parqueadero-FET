package com.fet.parqueadero.domain.model;

import java.util.List;
import java.util.Map;

/**
 * Catálogo estático de marcas y modelos más comunes en Colombia, clasificado
 * por tipo de vehículo y luego por marca, usado para alimentar dropdowns
 * encadenados en el frontend (tipo -> marca -> modelo). El campo {@code marca}
 * del vehículo sigue siendo texto libre: si el usuario no encuentra la suya
 * aquí, el frontend ofrece una opción "Otra" y la escribe manualmente.
 */
public final class CatalogoMarcas {

    private static final Map<TipoVehiculo, Map<String, List<String>>> CATALOGO = Map.of(
            TipoVehiculo.AUTOMOVIL, Map.ofEntries(
                    Map.entry("Audi", List.of("A3", "A4", "Q3", "Q5")),
                    Map.entry("BAIC", List.of("X25", "X35", "BJ40")),
                    Map.entry("BMW", List.of("Serie 3", "Serie 5", "X1", "X3")),
                    Map.entry("Changan", List.of("CS35", "CS55", "Alsvin")),
                    Map.entry("Chery", List.of("Tiggo 2", "Tiggo 7", "Arrizo 5")),
                    Map.entry("Chevrolet", List.of("Spark", "Onix", "Tracker", "Captiva")),
                    Map.entry("Citroën", List.of("C3", "C4 Cactus", "Berlingo")),
                    Map.entry("DFSK", List.of("Glory 580", "K01")),
                    Map.entry("Fiat", List.of("Mobi", "Argo", "Cronos")),
                    Map.entry("Ford", List.of("Fiesta", "Escape", "Explorer", "Ranger")),
                    Map.entry("Foton", List.of("Tunland", "Aumark")),
                    Map.entry("Great Wall (GWM)", List.of("Haval H6", "Poer")),
                    Map.entry("Honda", List.of("Civic", "CR-V", "HR-V", "City")),
                    Map.entry("Hyundai", List.of("Accent", "Tucson", "Santa Fe", "i10")),
                    Map.entry("JAC", List.of("S2", "S3", "T6")),
                    Map.entry("Jeep", List.of("Renegade", "Compass", "Wrangler")),
                    Map.entry("Kia", List.of("Picanto", "Rio", "Sportage", "Sorento")),
                    Map.entry("Mazda", List.of("Mazda2", "Mazda3", "CX-5", "CX-30")),
                    Map.entry("Mercedes-Benz", List.of("Clase A", "Clase C", "GLA", "GLC")),
                    Map.entry("Mitsubishi", List.of("Montero", "ASX", "L200")),
                    Map.entry("Nissan", List.of("Versa", "Sentra", "Qashqai", "X-Trail")),
                    Map.entry("Peugeot", List.of("208", "2008", "3008", "Partner")),
                    Map.entry("Renault", List.of("Logan", "Sandero", "Duster", "Kwid")),
                    Map.entry("SsangYong", List.of("Tivoli", "Korando", "Rexton")),
                    Map.entry("Subaru", List.of("Forester", "XV", "Outback")),
                    Map.entry("Suzuki", List.of("Swift", "Vitara", "Baleno")),
                    Map.entry("Toyota", List.of("Corolla", "Hilux", "RAV4", "Yaris", "Prado")),
                    Map.entry("Volkswagen", List.of("Gol", "Polo", "Tiguan", "Virtus")),
                    Map.entry("Volvo", List.of("XC40", "XC60", "XC90"))),
            TipoVehiculo.MOTOCICLETA, Map.ofEntries(
                    Map.entry("AKT", List.of("NKD 125", "TTR", "CR4", "Flex")),
                    Map.entry("Bajaj", List.of("Pulsar NS200", "Boxer", "Discover", "Dominar")),
                    Map.entry("Benelli", List.of("TNT 300", "Leoncino")),
                    Map.entry("Hero", List.of("Hunk", "Splendor")),
                    Map.entry("Honda", List.of("CB190R", "CBR500R", "XR150L")),
                    Map.entry("Kawasaki", List.of("Ninja 300", "Z400", "Versys")),
                    Map.entry("KTM", List.of("Duke 200", "Duke 390")),
                    Map.entry("Kymco", List.of("Agility", "Like 125")),
                    Map.entry("Royal Enfield", List.of("Classic 350", "Himalayan")),
                    Map.entry("Suzuki", List.of("GSX-S150", "V-Strom")),
                    Map.entry("TVS", List.of("Apache RTR 160", "Raider")),
                    Map.entry("UM", List.of("Renegade", "DSR")),
                    Map.entry("Victory", List.of("Octane", "Vegas")),
                    Map.entry("Yamaha", List.of("FZ", "MT-03", "XTZ 125", "YBR125"))),
            TipoVehiculo.OTRO, Map.of());

    private CatalogoMarcas() {
    }

    /** Marcas conocidas para el tipo de vehículo indicado (vacío para {@code OTRO}). */
    public static List<String> marcasPara(TipoVehiculo tipo) {
        return CATALOGO.getOrDefault(tipo, Map.of()).keySet().stream().sorted().toList();
    }

    /** Modelos conocidos para la marca y el tipo de vehículo indicados. */
    public static List<String> modelosPara(TipoVehiculo tipo, String marca) {
        return CATALOGO.getOrDefault(tipo, Map.of()).getOrDefault(marca, List.of());
    }

    /** Catálogo completo de modelos, agrupado por tipo de vehículo y luego por marca. */
    public static Map<TipoVehiculo, Map<String, List<String>>> todas() {
        return CATALOGO;
    }
}
