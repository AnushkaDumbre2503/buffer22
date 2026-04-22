# Intelligent Ad Allocation Engine

A comprehensive CLI-based ad allocation system built with Java and MySQL, implementing advanced DSA concepts and real-time ad bidding algorithms.

## 🎯 Features

### Core Engine
- **Real-time Ad Allocation**: Intelligent ad selection using scoring algorithms
- **Aho-Corasick Trie**: Fast keyword matching and context analysis
- **Max Heap Priority Queue**: Efficient ad selection based on scores
- **Conflict Graph**: Manages advertiser conflicts (Apple vs Samsung, Nike vs Adidas)
- **Memory Window**: Sliding window for tracking recent user interactions

### User Management
- User registration and authentication
- Session management with expiration
- Personalized ad allocation based on user context
- Search history tracking

### Advertiser Features
- Budget management and pacing
- Bid optimization
- Performance tracking (CTR, impressions, clicks)
- Conflict management with competitors
- Simulation engine for strategy testing

### DSA Implementation
- **Trie + Aho-Corasick**: O(n) keyword matching in text
- **Max Heap**: O(log n) insertion and extraction for best ad selection
- **Graph**: Conflict resolution using maximum independent set
- **Sliding Window**: Memory management for recent events

## 📁 Project Structure

```
ad-allocation-engine/
├── app/
│   └── Main.java                    # Entry point
├── cli/
│   └── CommandHandler.java          # CLI interface
├── model/                           # Data models
│   ├── User.java
│   ├── Advertiser.java
│   ├── Ad.java
│   ├── AdSlot.java
│   ├── AllocationResult.java
│   ├── AllocationEvent.java
│   └── Session.java
├── engine/                          # Core business logic
│   ├── AllocationEngine.java        # 🔥 CORE
│   ├── ScoringEngine.java
│   ├── CTRCalculator.java
│   ├── BudgetPacingEngine.java
│   ├── ConflictResolver.java
│   └── SimulationEngine.java
├── context/
│   ├── ContextMatcher.java
│   └── AhoCorasickTrie.java
├── dsa/                             # Data structures
│   ├── heap/MaxHeap.java
│   ├── graph/ConflictGraph.java
│   ├── trie/KeywordTrie.java
│   └── slidingwindow/MemoryWindow.java
├── manager/                         # Business managers
│   ├── UserManager.java
│   ├── AdvertiserManager.java
│   ├── SessionManager.java
│   └── SlotManager.java
├── service/
│   └── AdAllocationService.java     # Service layer
├── repository/                      # Database layer (JDBC)
│   ├── UserRepository.java
│   ├── AdvertiserRepository.java
│   ├── AdRepository.java
│   ├── AllocationRepository.java
│   └── SearchHistoryRepository.java
├── database/
│   ├── DBConnection.java
│   └── Schema.sql                   # Database schema
├── logs/
│   └── EventLogger.java
├── utils/
│   ├── Constants.java
│   └── TimeUtils.java
└── exceptions/
    └── AdEngineException.java
```

## 🚀 Setup Instructions

### Prerequisites
- Java 8 or higher
- MySQL Server
- MySQL JDBC Driver

### Database Setup

1. **Start MySQL Server**
   ```bash
   # On Windows
   net start mysql
   
   # On Linux/Mac
   sudo systemctl start mysql
   ```

2. **Create Database and Tables**
   ```bash
   mysql -u root -p < database/Schema.sql
   ```

3. **Update Database Credentials**
   - Edit `database/DBConnection.java` if needed
   - Default credentials: root / Wsrsid@75

### Compile and Run

1. **Add MySQL JDBC Driver to Classpath**
   ```bash
   # Download mysql-connector-java-8.0.xx.jar
   # Add to classpath when compiling
   ```

2. **Compile the Project**
   ```bash
   javac -cp ".;mysql-connector-java-8.0.xx.jar" app/Main.java
   ```

3. **Run the Application**
   ```bash
   java -cp ".;mysql-connector-java-8.0.xx.jar" app.Main
   ```

## 🎮 Usage Guide

### Main Menu Options

1. **Login** - Authenticate existing user
2. **Register** - Create new user account
3. **Global System View** - View system statistics
4. **Advertiser Mode** - Manage advertisers and ads
5. **Exit** - Quit application

### User Features

- **Search & Get Ads**: Enter search query to receive personalized ads
- **Click on Ad**: Record ad clicks for performance tracking
- **View My Ad History**: See your ad interaction history

### Advertiser Features

- **View All Advertisers**: List all advertisers with budgets
- **Create New Advertiser**: Add new advertiser with budget
- **View All Ads**: List all ads in the system
- **Create New Ad**: Create new ad with keywords and bid
- **Add Advertiser Conflict**: Set up conflicts between advertisers

## 🧠 Algorithm Details

### Ad Scoring Formula
```
Score = bid × CTR × slotWeight × contextMatch × memoryBoost × fatigueControl
```

### Slot Types
- **TOP**: High visibility (weight: 1.5, cooldown: 3s)
- **SIDEBAR**: Balanced (weight: 1.0, cooldown: 2s)
- **FOOTER**: Low visibility (weight: 0.7, cooldown: 1s)

### Auction Mechanism
- **Second-Price Auction**: Winner pays second-highest bid price
- **Budget Pacing**: Ensures budget lasts throughout the day
- **Conflict Resolution**: Uses maximum independent set algorithm

## 📊 Sample Data

The system comes pre-loaded with:

**Users:**
- john_doe, jane_smith, mike_wilson

**Advertisers:**
- Apple Inc ($10,000)
- Samsung Electronics ($8,000)
- Canon Inc ($5,000)
- Nike Inc ($6,000)
- Adidas AG ($5,500)

**Ads:**
- iPhone 15 Pro with AI Camera (Apple)
- Galaxy S24 Ultra (Samsung)
- EOS R5 Mirrorless Camera (Canon)
- Air Max 2024 (Nike)
- Ultraboost 22 (Adidas)

**Conflicts:**
- Apple ❌ Samsung
- Nike ❌ Adidas

## 🔍 Example Workflow

1. **User Login**
   ```
   Username: john_doe
   Email: john@example.com
   ```

2. **Search for Ads**
   ```
   Search query: AI camera iPhone
   Page content: Latest smartphone reviews and camera comparisons
   ```

3. **View Allocated Ads**
   ```
   🎯 Allocated Ads:
   TOP: iPhone 15 Pro with AI Camera (score: 8.25, price: $4.80)
   SIDEBAR: EOS R5 Mirrorless Camera (score: 3.20, price: $2.56)
   FOOTER: Air Max 2024 (score: 1.96, price: $1.57)
   ```

4. **Record Click**
   ```
   Enter Ad ID to click: 1
   ✅ Click recorded successfully!
   ```

## 🛠️ Technical Features

### Performance Optimizations
- **Aho-Corasick Algorithm**: O(n) pattern matching for keywords
- **Max Heap**: O(log n) ad selection
- **Memory Window**: Efficient sliding window for recent events
- **Connection Pooling**: Database connection management

### Error Handling
- Custom exception hierarchy
- Graceful database error handling
- Input validation and sanitization
- Comprehensive logging system

### Logging
- Event logging to file and console
- Performance metrics tracking
- Error logging with stack traces
- User activity auditing

## 🧪 Testing

The system includes sample data and can be tested immediately:

1. Run the application
2. Login with existing user (john_doe / john@example.com)
3. Try different search queries:
   - "AI camera iPhone" → Should show Apple ads
   - "running shoes" → Should show Nike/Adidas ads
   - "professional photography" → Should show Canon ads

## 📈 System Statistics

Monitor system performance through:
- Global revenue tracking
- Slot performance metrics
- CTR calculations
- Budget utilization rates
- Popular search terms

## 🔮 Future Enhancements

- Machine learning for bid optimization
- Real-time bidding integration
- Web-based dashboard
- Mobile app interface
- Advanced analytics and reporting
- A/B testing framework

## 📝 License

This project is for educational purposes to demonstrate DSA concepts in a real-world application.

## 🤝 Contributing

Feel free to extend the system with additional features or optimizations!
