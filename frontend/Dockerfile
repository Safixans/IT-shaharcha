# syntax=docker/dockerfile:1
#
# One parameterized image for any of the three Next.js apps in the workspace.
# Build context is the `frontend/` directory. Select the app with the APP build arg:
#   docker build -f frontend/Dockerfile --build-arg APP=public -t itsh/public frontend
# docker-compose.full.yml builds all three (public/console/learner) this way.

FROM node:22-alpine
WORKDIR /app
ARG APP
ENV APP=${APP}
ENV NEXT_TELEMETRY_DISABLED=1
# Next bakes the /api/* rewrite destination into the route manifest at build time,
# so the gateway URL must be set here (not just at runtime). On the compose network
# the gateway is reachable as api-gateway:8080; the hostname resolves at request time.
ARG GATEWAY_URL=http://api-gateway:8080
ENV GATEWAY_URL=${GATEWAY_URL}

# Install the whole workspace (shared packages + the chosen app), then build that app.
COPY package.json package-lock.json ./
COPY packages packages
COPY apps apps
RUN npm ci && npm run build -w @itsh/${APP}

# Each app's `start` script pins its own port (learner 3000 / console 3001 / public 3002).
CMD npm run start -w @itsh/$APP
