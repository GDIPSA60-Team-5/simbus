# Nimbus 🚌

> An intelligent public transportation companion app with AI-powered assistance


## 🚀 Setup Instructions

> **For Lecturers/Evaluators**: Please check and update the following configuration files:

### Configuration Files to Update

1. **Android App** (`android-kotlin/local.properties`):
   - Check `MAPS_API_KEY` for Google Maps integration

2. **Spring Backend**:
   - Check `spring-backend/application.properties` for database and API configurations
   - Check `spring-backend/.env` for environment-specific variables
   - **Update `ONEMAP_API_TOKEN`** - the provided token expires in 3 days

3. **Next.js Frontend** (`next-frontend/.env.local`):
   - Update backend URLs if running on different ports/hosts

4. **LLM Backend** (`llm-backend/.env`):
   - Update `BACKEND_URL` to match your backend deployment

---

## ✨ Features

### 🤖 AI-Powered Chatbot
- Natural language query processing for transit information
- Intent classification using TF-IDF and embeddings
- Multi-user conversation support
- Real-time route suggestions and bus timings

### 🗺️ Smart Navigation
- Real-time GPS tracking and route guidance
- Voice-guided turn-by-turn directions
- Offline map support with custom styling
- Bus arrival predictions and live updates

### 📱 Mobile Experience
- **Home Dashboard**: Quick access to saved routes and recent trips
- **Route Planning**: Intelligent multi-modal journey planning
- **Location Management**: Save frequently visited places
- **Trip History**: Track and analyze travel patterns
- **Push Notifications**: Alerts for bus arrivals and service updates

### 💻 Web Admin Dashboard
- User analytics and usage statistics
- Chatbot interaction monitoring
- Feedback management system
- Real-time system health monitoring
- Prometheus metrics integration

### 🔐 Security & Authentication
- JWT-based authentication
- Firebase integration for user management
- Secure API endpoints with Spring Security
- Role-based access control

---

## 🛠️ Technology Stack

### Mobile Application
- **Framework**: Android (Kotlin)
- **Architecture**: Multi-module clean architecture
- **UI**: Material Design components
- **Maps**: Google Maps SDK
- **Networking**: Retrofit, OkHttp
- **Authentication**: Firebase Auth

### Web Frontend
- **Framework**: Next.js 15
- **Language**: TypeScript
- **Styling**: Tailwind CSS 4
- **State Management**: React Context
- **Charts**: Custom chart components
- **Authentication**: JWT with HTTP-only cookies

### Backend Services
- **API Server**: Spring Boot 3.5 (Java 17)
- **Database**: MongoDB with Reactive Streams
- **Security**: Spring Security, JWT
- **Monitoring**: Spring Actuator, Prometheus
- **Messaging**: Firebase Cloud Messaging

### AI/ML Backend
- **Framework**: FastAPI (Python)
- **LLM**: GPT4All for local inference
- **NLP**: Sentence Transformers, scikit-learn
- **Intent Classification**: TF-IDF, embeddings
- **Data Processing**: Pandas, NumPy

### Infrastructure & DevOps
- **Containerization**: Docker, Docker Compose
- **Orchestration**: Ansible playbooks
- **Infrastructure**: Terraform
- **Monitoring**: Prometheus, Grafana
- **CI/CD**: GitHub Actions
- **Code Quality**: SonarCloud integration

---

## 🏗️ Architecture

### System Overview
```
┌─────────────────┐              ┌──────────────────┐
│   Android App   │              │   Next.js Web    │
│    (Kotlin)     │              │    Dashboard     │
└─────────┬───────┘              └────────┬─────────┘
          │                               │
          └───────────────┬───────────────┘
                          │
          ┌───────────────▼───────────────┐
          │       Spring Boot Backend     │
          │  (Authentication, CRUD, API)  │
          └─────────────┬─────────────────┘
                        │
          ┌─────────────▼─────────────┐ ┌─────────────────────────┐
          │        MongoDB           │ │       LLM Backend        │
          │   (User, Trip, Route)    │ │    (FastAPI + GPT4All)   │
          └──────────────────────────┘ └─────────────┬───────────┘
                                                     │
          ┌──────────────────────────────────────────▼─────────────┐
          │                External APIs                           │
          │      OneMap API, Firebase, Google Maps, Bus APIs      │
          └────────────────────────────────────────────────────────┘
```

### Key Design Patterns
- **Clean Architecture**: Separation of concerns across layers
- **Microservices**: Independent, scalable service components
- **Event-Driven**: Reactive programming with MongoDB Reactive Streams
- **API-First**: RESTful APIs with comprehensive documentation

---

## 🔄 CI/CD Pipeline

### Automated Workflows
- **Code Quality**: SonarCloud analysis on every PR
- **Testing**: Unit and integration tests
- **Security**: Dependency vulnerability scanning
- **Build**: Multi-platform Docker image builds
- **Deployment**: Automated staging deployments

### Quality Gates
- Test coverage > 80%
- Zero critical security vulnerabilities
- Code maintainability rating A
- Successful integration tests

---

## 📊 Monitoring & Analytics

### Application Monitoring
- **Metrics**: Prometheus with custom business metrics
- **Logging**: Structured logging with correlation IDs
- **Health Checks**: Spring Actuator endpoints
- **Performance**: Response time and throughput monitoring

### User Analytics
- **Usage Patterns**: Route planning frequency and preferences
- **Chatbot Metrics**: Query success rates and user satisfaction
- **Feature Adoption**: A/B testing for new features
- **Error Tracking**: Real-time error monitoring and alerting

---

## 🤝 Contributors

**GdipSA60 Team 5**

This project was developed as part of the Graduate Diploma in Software Analytics (GdipSA60) program at the National University of Singapore (NUS).

### Team Members
- [Phyo Nyi Nyi Paing](https://github.com/paulphyo)
- [Aung Myin Moe](https://github.com/Ammmoe)
- [Muhammad Haziq Bin Jamil](https://github.com/haziqjamil1)
- [Li Xing Bang](https://github.com/coderbang-bang)
- Zhang Rui
---

## 📄 License

This project is developed for educational purposes as part of the NUS GdipSA60 program.

---

## 🙏 Acknowledgments

- **OneMap API** for Singapore geospatial data
- **Land Transport Authority (LTA)** for real-time transport data
- **Firebase** for authentication and push notification services
- **Google Maps Platform** for mapping and location services

---

*Built with ❤️ by GdipSA60 Team 5*
