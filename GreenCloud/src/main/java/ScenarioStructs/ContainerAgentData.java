package ScenarioStructs;

import jade.core.AID;
import org.graphstream.graph.Graph;

import java.lang.ref.Reference;
import java.time.Duration;
import java.util.Random;

public class ContainerAgentData
{
    public String ContainerAgentName;
    public String ForecastAgentName;
    public String RegionalAgentName;
    public Duration ConnectionTime;
    public double BandwidthInMB;
    public double RAMInGB;
    public double MaxEnergyUsage;
    public double MaxEnergyProduction;
    public int CPUCores;
    public int Seed;

    public ContainerAgentData(String containerAgentName, String forecastAgentName, String regionalAgentName, Duration connectionTime, double bandwidthInMB,
                              double ramInGB, double maxEnergyUsage, double maxEnergyProduction,
                              int cpuCores)
    {
        ContainerAgentName = containerAgentName;
        ForecastAgentName = forecastAgentName;
        RegionalAgentName = regionalAgentName;
        ConnectionTime = connectionTime;
        BandwidthInMB = bandwidthInMB;
        RAMInGB = ramInGB;
        MaxEnergyUsage = maxEnergyUsage;
        MaxEnergyProduction = maxEnergyProduction;
        CPUCores = cpuCores;
        Random rand = new Random();
        Seed = rand.nextInt();
    }
    public ContainerAgentData(String containerAgentName, String forecastAgentName, String regionalAgentName, Duration connectionTime, double bandwidthInMB,
                              double ramInGB, double maxEnergyUsage, double maxEnergyProduction,
                              int cpuCores, int seed)
    {
        ContainerAgentName = containerAgentName;
        ForecastAgentName = forecastAgentName;
        RegionalAgentName = regionalAgentName;
        ConnectionTime = connectionTime;
        BandwidthInMB = bandwidthInMB;
        RAMInGB = ramInGB;
        MaxEnergyUsage = maxEnergyUsage;
        MaxEnergyProduction = maxEnergyProduction;
        CPUCores = cpuCores;
        Seed = seed;
    }
}
