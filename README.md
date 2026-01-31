# Cafeteria Simulation Engine

A Java-based discrete event simulation system for modeling and analyzing cafeteria operations with a graphical user interface.

## 📋 Overview

This project implements a comprehensive simulation engine designed to model cafeteria operations, customer flows, and service efficiency. Built with Java and featuring an intuitive GUI, it allows users to configure, run, and analyze various cafeteria scenarios to optimize service delivery and resource allocation.

## 🌐 Live Documentation

Full JavaDoc documentation is available at: [https://mustah21.github.io/Java-simulator-project/](https://mustah21.github.io/Java-simulator-project/)

## ✨ Features

- **Discrete Event Simulation**: Accurate modeling of time-based events in a cafeteria environment
- **Graphical User Interface**: User-friendly interface for configuring and monitoring simulations
- **CSV Export**: Export simulation results for further analysis
- **Configurable Parameters**: Customize arrival rates, service times, and resource allocation
- **Real-time Visualization**: Monitor simulation progress and metrics
- **Statistical Analysis**: Generate insights from simulation runs

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven (optional, for dependency management)
- An IDE (IntelliJ IDEA, Eclipse, or VS Code recommended)

### Installation

1. Clone the repository:
```bash
git clone https://github.com/mustah21/Java-simulator-project.git
cd Java-simulator-project
```

2. Open the project in your preferred IDE

3. Build the project:
```bash
javac -d bin Simu_GUI/**/*.java
```

### Running the Simulation

1. Navigate to the project directory
2. Run the main class:
```bash
java -cp bin <MainClassName>
```

Alternatively, use your IDE's run configuration to launch the application.

## 📁 Project Structure

```
Java-simulator-project/
├── Simu_GUI/              # GUI components and main application
├── docs/                  # JavaDoc documentation
├── .idea/                 # IntelliJ IDEA configuration
├── CAFETERIA_ENGINE_BACKLOG.md  # Project backlog and tasks
├── SimulationResults.csv  # Sample simulation output
└── README.md              # Project documentation
```

## 🎯 Usage

### Basic Workflow

1. **Launch the Application**: Start the GUI application
2. **Configure Parameters**: Set simulation parameters such as:
   - Customer arrival rate
   - Service time distributions
   - Number of service points
   - Simulation duration
3. **Run Simulation**: Execute the simulation with chosen parameters
4. **Analyze Results**: View real-time statistics and export results to CSV
5. **Optimize**: Adjust parameters based on results to find optimal configurations

### Example Scenarios

- **Peak Hour Analysis**: Simulate lunch rush to determine optimal staffing
- **Queue Management**: Test different queue configurations for efficiency
- **Resource Allocation**: Determine the ideal number of service points
- **Capacity Planning**: Evaluate system performance under various loads

## 📊 Output and Analysis

Simulation results are exported to `SimulationResults.csv` containing:
- Timestamp data
- Queue lengths
- Service times
- Utilization rates
- Customer wait times
- Throughput metrics

## 🛠️ Technology Stack

- **Language**: Java (95.8%)
- **UI Styling**: CSS (4.2%)
- **Documentation**: JavaDoc
- **Build Tool**: Compatible with Maven/Gradle

## 📚 Documentation

Comprehensive API documentation is generated using JavaDoc and hosted on GitHub Pages. Visit the [documentation site](https://mustah21.github.io/Java-simulator-project/) for detailed information about:
- Class hierarchy
- Method signatures
- Parameter descriptions
- Usage examples

## 🗓️ Development

This project follows an agile development methodology with a maintained backlog in `CAFETERIA_ENGINE_BACKLOG.md`. 

### Building JavaDoc

To regenerate the documentation:
```bash
javadoc -d docs -sourcepath Simu_GUI -subpackages .
```

## 🤝 Contributing

Contributions are welcome! To contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style

- Follow Java naming conventions
- Document all public methods and classes
- Include JavaDoc comments for clarity
- Write unit tests for new features

## 📝 Project Backlog

Track ongoing development and planned features in `CAFETERIA_ENGINE_BACKLOG.md`

## 🧪 Testing

Run tests using your IDE's test runner or via command line:
```bash
# Example using Maven
mvn test
```

## 📄 License

This project is available for educational and academic purposes.

## 👥 Contributors

- [@mustah21](https://github.com/mustah21)
- Additional contributors welcome!

## 🙏 Acknowledgments

- Discrete Event Simulation principles and methodologies
- Java GUI design patterns
- Statistical analysis techniques

## 📧 Support

For questions, issues, or suggestions:
- Open an issue on GitHub
- Check the [documentation](https://mustah21.github.io/Java-simulator-project/)
- Review the project backlog for known issues

## 🔮 Future Enhancements

Potential features for future development:
- Multi-cafeteria simulation support
- Advanced statistical analysis tools
- Machine learning-based optimization
- Mobile/web-based interface
- Real-time data integration
- Enhanced visualization options

---

**Note**: This simulation engine is designed for educational and analytical purposes to understand cafeteria operations and optimization strategies.
