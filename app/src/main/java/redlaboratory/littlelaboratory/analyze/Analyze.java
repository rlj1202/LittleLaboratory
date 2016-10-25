package redlaboratory.littlelaboratory.analyze;

import java.util.List;

public interface Analyze {

    AnalyzeDerivative ANALYZE_DERIVATIVE = new AnalyzeDerivative();
    AnalyzeIntegral ANALYZE_INTEGRAL = new AnalyzeIntegral();

    Analyze[] values = {ANALYZE_DERIVATIVE, ANALYZE_INTEGRAL};

    List<Double> analyze(List<Double> data);

    int getName();

}
