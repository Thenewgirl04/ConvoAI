# Use Node.js runtime
FROM node:20-alpine

# Set working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy the rest of the frontend source code
COPY . .

# Expose frontend port
EXPOSE 5173

# Start the React app
CMD ["npm", "run", "dev"]

