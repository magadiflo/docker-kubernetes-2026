# ============================================
# Stage 1: Build the Angular Application
# ============================================
ARG NODE_VERSION=24.18.1-alpine
ARG NGINX_VERSION=alpine3.22

# Utiliza una imagen ligera de Node.js para compilar la aplicación.
# La versión puede personalizarse mediante el ARG NODE_VERSION.
FROM node:${NODE_VERSION} AS builder
# Establece /app como directorio de trabajo dentro del contenedor.
WORKDIR /app
# Copia primero los archivos de dependencias para aprovechar la caché de Docker.
COPY package.json package-lock.json ./
# Instala las dependencias de forma limpia y reproducible mediante npm ci.
# Utiliza una caché de npm para acelerar compilaciones posteriores.
RUN --mount=type=cache,target=/root/.npm npm ci
# Copia el resto del código fuente de la aplicación en el contenedor.
COPY . .
# Compila la aplicación Angular para producción.
RUN npm run build

# ============================================
# Stage 2: Prepare Nginx to Serve Static Files
# ============================================
FROM nginxinc/nginx-unprivileged:${NGINX_VERSION} AS runner
# Copia la configuración personalizada de Nginx.
COPY nginx.conf /etc/nginx/nginx.conf
# Cambia a root para realizar las operaciones administrativas necesarias
# durante la construcción de la imagen.
USER root
# Copia los archivos generados durante el build de Angular al directorio
# que Nginx utiliza para servir contenido estático.
COPY --from=builder /app/dist/*/browser /usr/share/nginx/html
# Asigna nginx como propietario y grupo de todo el contenido de /html
# (directorio, subdirectorios y archivos gracias a -R, que indica recursividad).
# USER root permite realizar este cambio de propietarios.
RUN chown -R nginx:nginx /usr/share/nginx/html
# Copia el script de inicialización que configurará las variables
# de entorno en tiempo de ejecución.
COPY configure-runtime-env.sh /configure-runtime-env.sh
# Convierte los saltos de línea de Windows (CRLF) a Unix (LF) para evitar
# problemas de ejecución y marca el script como ejecutable.
RUN sed -i 's/\r$//' /configure-runtime-env.sh && chmod +x /configure-runtime-env.sh

# Cambia a un usuario sin privilegios para ejecutar el contenedor
# y seguir las mejores prácticas de seguridad.
USER nginx
# Documenta que el contenedor utiliza el puerto 8080 para el tráfico HTTP.
# La imagen nginx-unprivileged utiliza el puerto 8080 en lugar del puerto 80.
# EXPOSE no publica el puerto; únicamente documenta el puerto utilizado por el contenedor.
EXPOSE 8080
# Ejecuta el script de inicialización al iniciar el contenedor.
# El script finaliza con exec "$@", por lo que ejecutará el comando definido en CMD.
ENTRYPOINT ["/configure-runtime-env.sh"]
# Inicia Nginx utilizando la configuración personalizada y lo mantiene
# ejecutándose en primer plano como proceso principal del contenedor.
CMD ["nginx", "-c", "/etc/nginx/nginx.conf", "-g", "daemon off;"]
