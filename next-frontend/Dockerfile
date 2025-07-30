FROM node:18-alpine

WORKDIR /app

# Copy package.json and package-lock.json for npm
COPY package.json package-lock.json ./

# Install dependencies with npm ci (clean install)
RUN npm ci

# Copy rest of the source code
COPY . .

# Build the Next.js app
RUN npm run build

# Expose default Next.js port
EXPOSE 3000

# Start the app
CMD ["npm", "start"]
