# syntax=docker.io/docker/dockerfile:1

FROM node:22-alpine AS base

# Install dependencies only when needed
FROM base AS builder
# Check https://github.com/nodejs/docker-node/tree/b4117f9333da4138b03a546ec926ef50a31506c3#nodealpine to understand why libc6-compat might be needed.
RUN apk add --no-cache libc6-compat
WORKDIR /app/lo3web

# Install dependencies based on the preferred package manager
COPY ../lo3web/package.json ../lo3web/package-lock.json* ../lo3web/.npmrc* ./
RUN npm ci

COPY ../lo3web .
COPY ../.env ./

RUN npm run build

# Production image, copy all the files and run next
FROM base AS runner
WORKDIR /app/lo3web

COPY --from=deps /app/lo3web/node_modules ./node_modules
COPY ../lo3web/build build/
COPY ../lo3web/package.json ./
COPY ../.env ./

ENV PORT=3000
EXPOSE 3000
ENV NODE_ENV=production
# ENTRYPOINT npm run dev
CMD [ "node","-r", "--env-file=.env", "build" ]