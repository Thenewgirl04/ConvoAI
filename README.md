# ConvoAI - Intelligent Chatbot Assistant

## Project Description

ConvoAI is a full-stack web application that provides an intelligent conversational AI assistant with **two distinct modes**:

1. **Default Mode**: A general-purpose helpful assistant
2. **Therapy Mode**: A supportive, non-judgmental listener designed to provide empathetic support (not a replacement for professional mental health services)

The application maintains conversation history per session, allowing users to have continuous, contextual conversations. Built with a modern React frontend and Spring Boot backend, it integrates with OpenAI's API to deliver intelligent responses.

---

## Features

### Core Features

✅ **Dual Conversation Modes**

- Toggle between General Assistant and Therapy Mode
- Each mode has distinct system prompts and behavioral guidelines

✅ **Session Management**

- Persistent conversation history per session
- Automatic session ID generation and management
- Session data stored in browser's localStorage
- "New Session" button to start fresh conversations

✅ **Real-time Chat Interface**

- Modern, responsive chat UI built with React & Bootstrap
- Auto-scrolling chat messages
- Type-to-send with Enter key support
- Visual distinction between user and bot messages

✅ **Context-Aware Responses**

- Maintains full conversation history
- AI responds based on complete conversation context
- Supports multi-turn conversations

✅ **Error Handling**

- Graceful error messages for API failures
- Validation for empty prompts
- Connection error handling

✅ **CORS Support**

- Cross-origin requests enabled for flexible deployment

### Therapy Mode Features

- Specialized system prompt for supportive listening
- Brief, reflective responses
- Open-ended questions to deepen understanding
- Crisis detection capability (mentions 988 helpline)
- Professional boundaries (no diagnoses or medical advice)

---

## Technology Stack

### Frontend

- **Framework**: React 19
- **Build Tool**: Vite 7
- **Styling**: Bootstrap 5
- **HTTP Client**: Axios
- **Language**: JavaScript (JSX)

### Backend

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Build Tool**: Maven
- **HTTP Client**: Spring RestClient
- **Session Storage**: ConcurrentHashMap (in-memory)

### Deployment

- **Containerization**: Docker & Docker Compose
- **External API**: OpenAI API (gpt-5)

---

## Installation & Setup

### Prerequisites

**Option 1: Docker (Recommended)**

- Docker Desktop 4.0+
- Docker Compose 2.0+

**Option 2: Local Development**

- Java 21 or higher
- Maven 3.6+
- Node.js 18+ & npm

**For Both Options**

- OpenAI API Key (get it from: https://platform.openai.com/api-keys)

---

## Running the Application

### Option 1: Docker Compose (Easiest)

1. **Clone the repository**

   ```bash
   cd ConvoAI-main
   ```

2. **Set up environment variables**

   - Edit `sb-chatgpt/sb-chatgpt/src/main/resources/application.properties`
   - Add your OpenAI API key:
     ```properties
     openapi.api.key = your-api-key-here
     openapi.api.model = gpt-4  # or gpt-5, gpt-3.5-turbo, etc.
     ```

3. **Start the application**

   ```bash
   docker-compose up --build
   ```

4. **Access the application**

   - Frontend: http://localhost:5173
   - Backend API: http://localhost:8080/api/chat

5. **Stop the application**
   ```bash
   docker-compose down
   ```

---

### Option 2: Local Development

#### Backend Setup (Spring Boot)

1. **Navigate to backend directory**

   ```bash
   cd sb-chatgpt/sb-chatgpt
   ```

2. **Configure API credentials**

   - Edit `src/main/resources/application.properties`
   - Add your OpenAI API key and model:
     ```properties
     openapi.api.key = your-api-key-here
     openapi.api.model = gpt-4
     ```

3. **Build the project**

   ```bash
   mvn clean install
   ```

4. **Run the backend**
   ```bash
   mvn spring-boot:run
   ```
   - Backend will start on: http://localhost:8080

#### Frontend Setup (React)

1. **In a new terminal, navigate to frontend directory**

   ```bash
   cd chatgpt-react
   ```

2. **Install dependencies**

   ```bash
   npm install
   ```

3. **Start development server**

   ```bash
   npm run dev
   ```

   - Frontend will start on: http://localhost:5173

4. **Build for production (optional)**
   ```bash
   npm run build
   ```

#### Lint the frontend (optional)

```bash
npm run lint
```

---

## API Endpoints

### Chat Endpoint

**POST** `/api/chat`

**Request Body**

```json
{
  "prompt": "Your message here",
  "mode": "default", // or "therapy"
  "sessionId": "existing-session-id-or-null"
}
```

**Response**

```json
{
  "sessionId": "unique-session-uuid",
  "reply": "AI assistant response"
}
```

**Error Response**

```json
{
  "sessionId": null,
  "reply": "Error message description"
}
```

---

## Configuration

### Backend Configuration

Edit `sb-chatgpt/sb-chatgpt/src/main/resources/application.properties`:

```properties
# OpenAI API Configuration
openapi.api.url = https://api.openai.com/v1/chat/completions
openapi.api.model = gpt-4              # Model to use
openapi.api.key = your-api-key         # Your OpenAI API key

# Server Configuration
spring.application.name = sb-chatgpt
server.port = 8080                     # Backend port

# Logging
logging.level.org.springframework.web = DEBUG
```

### Frontend Configuration

The frontend connects to the backend at `http://localhost:8080` by default. To change this:

Edit `chatgpt-react/src/components/Chatbot.jsx`:

```javascript
// Look for this line and modify the URL:
const res = await axios.post(
  "http://localhost:8080/api/chat",  // ← Change this
  body,
  ...
);
```

---

## Project Structure

```
ConvoAI-main/
├── docker-compose.yml              # Docker orchestration
├── chatgpt-react/                  # React Frontend
│   ├── src/
│   │   ├── App.jsx                # Main app component
│   │   ├── components/
│   │   │   └── Chatbot.jsx        # Chat UI & logic
│   │   ├── index.css
│   │   └── main.jsx
│   ├── package.json
│   ├── vite.config.js
│   └── Dockerfile
├── sb-chatgpt/                     # Spring Boot Backend
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/springboot/sb_chatgpt/
│       │   │   ├── SbChatgptApplication.java
│       │   │   ├── controller/
│       │   │   │   └── ChatGPTController.java
│       │   │   ├── service/
│       │   │   │   └── ChatGPTService.java
│       │   │   ├── dto/
│       │   │   │   ├── ChatResponse.java
│       │   │   │   ├── PromptRequest.java
│       │   │   │   ├── ChatGPTRequest.java
│       │   │   │   └── ChatGPTResponse.java
│       │   │   └── config/
│       │   │       └── OpenAPIConfiguration.java
│       │   └── resources/
│       │       └── application.properties
│       └── test/
└── REQUIREMENTS.txt
```

---

## How It Works

### Session Flow

1. **User Interaction**

   - User types a message in the React chatbot interface

2. **Frontend Processing**

   - Message is sent to backend via HTTP POST request
   - Current session ID is included (or undefined for new session)

3. **Backend Processing**

   - Backend receives the message
   - If new session: generates UUID, initializes with system prompt
   - Adds user message to session history
   - Sends complete message history to OpenAI API

4. **AI Response**

   - OpenAI processes the message sequence
   - Returns contextual response based on full conversation
   - Response is added to session history

5. **Response to Frontend**

   - Session ID and AI reply sent back to React app
   - Frontend displays reply and stores session ID

6. **Session Persistence**
   - Session ID saved to browser's localStorage
   - On next visit, same session ID retrieves previous conversation

---

## System Prompt Behavior

### Default Mode

```
"You are a helpful, concise assistant."
```

### Therapy Mode

```
"You are a supportive, non-judgemental listener. You are NOT a licensed clinician.
Style: brief reflections, open questions, warmth, concise.
Steps:
1) Reflect what you heard in one sentence.
2) Ask one open question to deepen understanding.
3) Offer one short coping idea ONLY if user asks or gives permission.
4) If user shows signs of improvement, you may proceed to conclude the conversation.
Boundaries: no diagnoses, no medical advice.
Crisis: if you detect imminent self-harm or harm to others, stop normal replies and show crisis info (988 in the U.S)."
```

---

## Known Limitations

⚠️ **Session Persistence**: Sessions are stored in-memory and will be lost when the backend restarts. For production, implement a database (MySQL, PostgreSQL, MongoDB, etc.).

⚠️ **Therapy Mode Disclaimer**: This is NOT a substitute for professional mental health services. It's designed as a supportive tool only.

⚠️ **API Rate Limiting**: Subject to OpenAI's rate limits. Monitor your API usage and costs.

⚠️ **Authentication**: Currently no user authentication. Any client can call the API.

---

## Future Enhancements

- Database integration for persistent sessions
- User authentication and authorization
- Multi-user support with user profiles
- Conversation history export
- Fine-tuned models for specialized use cases
- Conversation analytics dashboard
- Rate limiting and quota management
- Multiple AI provider support (Claude, Gemini, etc.)

---

## Troubleshooting

### Backend won't start

- Ensure Java 21 is installed: `java -version`
- Check if port 8080 is available: `netstat -an | findstr 8080` (Windows)
- Verify Maven is installed: `mvn -version`

### Frontend won't start

- Ensure Node.js 18+ is installed: `node --version`
- Clear npm cache: `npm cache clean --force`
- Delete node_modules and reinstall: `rm -rf node_modules && npm install`

### API Key errors

- Verify your OpenAI API key is valid
- Check that the key has appropriate permissions
- Ensure the key is not in the git repository (use environment variables in production)

### CORS errors

- Backend has `@CrossOrigin("*")` enabled
- Ensure frontend is using correct backend URL in axios call

### Docker issues

- Ensure Docker Desktop is running
- Check Docker logs: `docker-compose logs`
- Rebuild containers: `docker-compose up --build`

---

## License

This project is open source. See LICENSE file for details.

---

## Support

For issues or questions:

1. Check the troubleshooting section
2. Review the code comments in key files
3. Check OpenAI API documentation: https://platform.openai.com/docs

---

## Security Considerations

🔒 **Important**:

- Never commit your OpenAI API key to version control
- Use environment variables in production
- Implement authentication for production deployments
- Add rate limiting to prevent abuse
- Validate and sanitize all user inputs
- Use HTTPS in production
- Implement proper CORS policies (restrict from `*`)

---

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

**Last Updated**: January 2026
