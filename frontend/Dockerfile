FROM nginx:stable-alpine3.24
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf
COPY src/main/resources/static/ /usr/share/nginx/html/
EXPOSE 80
