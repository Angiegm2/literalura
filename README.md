# Challenge LiterAlura con API Gutendex

Este repositorio contiene el desarrollo de un proyecto basado en la API [Gutendex](https://gutendex.com/), diseñado como parte de un challenge de programación. El objetivo es construir una aplicación que permita explorar y gestionar datos de libros digitalizados de manera eficiente.

## Tecnologías Utilizadas

- **Lenguaje:** Java
- **IDE:** IntelliJ IDEA
- **API:** Gutendex (Acceso a datos de libros del Proyecto Gutenberg)
- **Control de versiones:** Git y GitHub
- **Base de Datos:** PostgreSQL (para almacenamiento y gestión de datos)

## Objetivos del Proyecto

1. Conectar y consumir datos de la API Gutendex.
2. Implementar funcionalidades como búsqueda, filtrado y visualización de libros.
3. Aplicar buenas prácticas de programación y principios de desarrollo Back-end.
4. Integrar PostgreSQL para almacenamiento persistente de datos.

## Estructura del Proyecto

La estructura del proyecto estará organizada en los siguientes módulos:

- **Módulo de Conexión a la API:** Responsable de realizar las solicitudes HTTP y procesar las respuestas.
- **Módulo de Lógica de Negocio:** Implementará las funcionalidades principales como búsqueda y filtrado.
- **Módulo de Presentación:** Gestionará la interfaz de usuario (CLI o interfaz gráfica según corresponda).
- **Base de Datos:** Contendrá la configuración y la conexión a la base de datos PostgreSQL.
- **Pruebas:** Contendrá las pruebas unitarias y de integración para asegurar la calidad del código.

## Cómo Ejecutar el Proyecto

1. Clona este repositorio:
   ```bash
   git clone https://github.com/tu_usuario/challenge-literalura.git
   ```
2. Abre el proyecto en IntelliJ IDEA.
3. Configura el entorno de ejecución según las instrucciones del proyecto.
4. Configura la conexión a PostgreSQL. Asegúrate de tener PostgreSQL instalado y de crear una base de datos para el proyecto.
5. Ejecuta la aplicación y comienza a explorar los libros.

## Notas Adicionales

- Este proyecto es parte de un challenge educativo, por lo que se prioriza el aprendizaje sobre la complejidad.
- Se recomienda revisar la documentación de la API Gutendex para entender las capacidades y limitaciones del servicio.
- Las mejoras futuras incluirán la optimización del código, nuevas funcionalidades y soporte para múltiples idiomas.
