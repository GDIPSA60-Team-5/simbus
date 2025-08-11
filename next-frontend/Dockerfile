# Build stage
FROM node:24-alpine3.21 AS builder

WORKDIR /app

COPY package.json package-lock.json ./

RUN npm ci

COPY . .

RUN npm run build

# Production stage
FROM node:18-alpine AS runner

WORKDIR /app

COPY --from=builder /app/package.json /app/package-lock.json ./
RUN npm ci --only=production

COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

EXPOSE 3000

CMD ["npm", "start"]
