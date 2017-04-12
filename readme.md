# Internship April 2016/June 2016

This is the github repository for the internship
## Structure
Here are three separate folders:
  - TDLearning : A Temporal difference learning agent, with a swing interface showing the evaluated values for each state
  - qLearning_Sarsa: a Q-Learning and a SARSA Agent. The interface Allows the user to modify the environment (rewards, obstacles). The interface updates as the running agent updates his perceived values. Also allows multiple agents to run concurrently, although only one will have his values propagated to the interface
  - chartGen: C# tool used to create an Excel file containing charts with the detail of the perceived rewards for each agent. This tool can be called automatically from the qLearning_Sarsa application
 
You can also find the french memoire detailing the work done and the process followed.

## Use
The C# tool uses EPPlus to write to excel files. It might be necesary to install it. The project is a visual studio solution.
The other two applications are built through an eclipse project. To be used, you need the Jason Eclipse Plug-in and the Jason JAR (version 1.4.2 was used for development). If you do not wish to modify the code, you can simply run the applications with the JAR at the root of the project directory.
    


