# Build stage
FROM node:16-alpine as build
WORKDIR /app

ARG REACT_APP_FIREBASE_API_KEY
ARG REACT_APP_FIREBASE_AUTH_DOMAIN
ARG REACT_APP_FIREBASE_PROJECT_ID
ARG REACT_APP_FIREBASE_STORAGE_BUCKET
ARG REACT_APP_FIREBASE_MESSAGING_SENDER_ID
ARG REACT_APP_FIREBASE_APP_ID

COPY package*.json ./
RUN npm install
COPY . .

# Update service URLs to access via nginx proxy
RUN sed -i 's|const USER_API_URL = .*|const USER_API_URL = "/user";|g' ./src/services/authService.js
RUN sed -i 's|const EVENT_API_URL = .*|const EVENT_API_URL = "/event";|g' ./src/services/eventService.js
RUN sed -i 's|const BOOKING_API_URL = .*|const BOOKING_API_URL = "/booking";|g' ./src/services/bookingService.js
RUN sed -i 's|const EXTERNAL_API_URL = .*|const EXTERNAL_API_URL = "/external";|g' ./src/services/mapService.js
RUN sed -i 's|const EXTERNAL_API_URL = .*|const EXTERNAL_API_URL = "/external";|g' ./src/services/cityInfoService.js
RUN sed -i 's|const EXTERNAL_API_URL = .*|const EXTERNAL_API_URL = "/external";|g' ./src/services/currencyService.js

RUN npm run build

# Production stage
FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]