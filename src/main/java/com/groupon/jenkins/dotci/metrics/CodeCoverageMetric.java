package com.groupon.jenkins.dotci.metrics;

import com.groupon.jenkins.dynamic.build.DynamicBuild;
import com.groupon.jenkins.dynamic.build.api.metrics.JobMetric;
import com.groupon.jenkins.dynamic.build.api.metrics.charts.Chart;
import com.groupon.jenkins.dynamic.build.api.metrics.charts.ChartColor;
import com.groupon.jenkins.dynamic.build.api.metrics.charts.LineChart;
import hudson.Extension;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.Ratio;
import hudson.plugins.cobertura.targets.CoverageMetric;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Extension
public class CodeCoverageMetric extends JobMetric {
    @Override
    public Chart getChart() {
        List<DynamicBuild> builds = getCoverageBuilds();
        List<String> buildNumbers = new ArrayList<String>();
        List<Long> lineCoverages= new ArrayList<Long>();
        List<Long> methodsCoverages= new ArrayList<Long>();
        List<Long> packagesCoverages= new ArrayList<Long>();
        List<Long> filesCoverages= new ArrayList<Long>();
        for(DynamicBuild build: builds){
            buildNumbers.add(build.getNumber() +"") ;
            CoberturaBuildAction coberturaAction = build.getAction(CoberturaBuildAction.class);;
            Ratio lineCoverage = coberturaAction.getResult().getCoverage(CoverageMetric.LINE);
            lineCoverages.add(new Long(lineCoverage.getPercentage()));

            Ratio methodCoverage = coberturaAction.getResult().getCoverage(CoverageMetric.METHOD);
            methodsCoverages.add(new Long(methodCoverage.getPercentage()));

            Ratio packageCoverage = coberturaAction.getResult().getCoverage(CoverageMetric.PACKAGES);
            packagesCoverages.add(new Long(packageCoverage.getPercentage()));

            Ratio filesCoverage = coberturaAction.getResult().getCoverage(CoverageMetric.FILES);
            filesCoverages.add(new Long(filesCoverage.getPercentage()));
        }
        return new LineChart(buildNumbers,
                Arrays.asList(
                        new LineChart.DataSet("Line", lineCoverages, ChartColor.BLUE),
                        new LineChart.DataSet("Method", methodsCoverages, ChartColor.YELLOW),
                        new LineChart.DataSet("Packages", packagesCoverages,ChartColor.GREEN),
                        new LineChart.DataSet("Files", filesCoverages, ChartColor.RED)
                ),
                "Build Number","Coverage(%)");
    }



    private List<DynamicBuild> getCoverageBuilds() {
        Query<DynamicBuild> query = getQuery().filter("actions.className", CoberturaBuildAction.class.getName()).limit(getBuildCount());
        return getBuilds(query);
    }




    @Override
    public String getTitle() {
        return "Code Coverage";
    }

    @Override
    public boolean isApplicable() {
        return getCoverageBuilds().size() > 0 ;
    }

}
