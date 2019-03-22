package edu.mayo.bsi.nlp2fhir.pipelines;

import edu.mayo.bsi.nlp2fhir.evaluation.EvaluationXMIReader;
import edu.mayo.bsi.nlp2fhir.pipelines.evaluation.EvaluationPipelineBuilder;
import edu.mayo.bsi.nlp2fhir.pipelines.resources.ResourcePipelineBuilder;
import edu.mayo.bsi.nlp2fhir.pipelines.serialization.SerializationPipelineBuilder;
import edu.mayo.bsi.nlp2fhir.postprocessors.XMIWriterPostProcessor;
import edu.mayo.bsi.nlp2fhir.preprocessors.FHIRFileSystemReader;
import edu.mayo.bsi.nlp2fhir.specialized.SectionReader;
import edu.mayo.bsi.nlp2fhir.evaluation.EvaluationXMIReader;
import edu.mayo.bsi.nlp2fhir.evaluation.SHARPKnowtatorXMLReaderWrapper;
import edu.mayo.bsi.nlp2fhir.pipelines.evaluation.EvaluationPipelineBuilder;
import edu.mayo.bsi.nlp2fhir.pipelines.resources.ResourcePipelineBuilder;
import edu.mayo.bsi.nlp2fhir.pipelines.serialization.SerializationPipelineBuilder;
import edu.mayo.bsi.nlp2fhir.preprocessors.FHIRFileSystemReader;
import edu.mayo.bsi.nlp2fhir.preprocessors.FHIRXMIFileSystemReader;
import edu.mayo.bsi.nlp2fhir.specialized.SectionReader;
import edu.mayo.bsi.nlp2fhir.transformers.CTAKESToFHIRFamilyMemberHistory;
import edu.mayo.bsi.nlp2fhir.transformers.CTAKESToFHIRProblemList;
import org.apache.ctakes.assertion.pipelines.GoldEntityAndAttributeReaderPipeline;
import org.apache.ctakes.core.ae.SHARPKnowtatorXMLReader;
import org.apache.ctakes.core.cr.XmiCollectionReaderCtakes;
import org.apache.ctakes.relationextractor.data.GoldAnnotationAnalysisPipeline;
import org.apache.ctakes.typesystem.type.structured.DocumentID;
import org.apache.uima.UIMAException;
import org.apache.uima.UIMAFramework;
import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.CAS;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AggregateBuilder;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.internal.ResourceManagerFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.resource.ResourceManager;
import org.apache.uima.util.CasCopier;
import org.apache.uima.util.CasCreationUtils;
import org.hl7.fhir.Condition;
import org.hl7.fhir.FamilyMemberHistory;
import org.hl7.fhir.Procedure;
import org.ohnlp.medtagger.cr.FileSystemReader;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;
import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;

public class LiweiPipeline {
    public static void main(String... args) throws IOException, UIMAException {
        CollectionReaderDescription cr = CollectionReaderFactory.createReaderDescription(
                FHIRFileSystemReader.class,
                FileSystemReader.PARAM_INPUTDIR, "resources/evaluation/sharpn/text"
        );
        // Pipeline to create gold standard
        AggregateBuilder goldPipeline = new AggregateBuilder();
        goldPipeline.add(AnalysisEngineFactory.createEngineDescription(SHARPKnowtatorXMLReaderWrapper.class,
                SHARPKnowtatorXMLReader.PARAM_SET_DEFAULTS, false,
                SHARPKnowtatorXMLReader.PARAM_TEXT_DIRECTORY, "resources/evaluation/sharpn/text"));
//        goldPipeline.add(AnalysisEngineFactory.createEngineDescription(SectionReader.class,
//                SectionReader.PARAM_PROJECT_FILE, "resources/evaluation/sharpn/sections/SECTION_ANNOTATIONS.pprj"));
//        goldPipeline.add(ResourcePipelineBuilder
//                .newBuilder()
//                .addProblemListResources()
//                .addFamilyHistoryResources()
//                .build());
//        goldPipeline.add(SerializationPipelineBuilder
//                .newBuilder(new File("gold_out"))
//                .addKnowtatorOutput("Condition", "Procedure", "Device", "MedicationStatement", "FamilyMemberHistory")
//                .addXMIOutput()
//                .addAnaforaOutput("anafora", "SHARPN", "Composition", "Condition", "Procedure", "MedicationStatement", "FamilyMemberHistory")
//                .addFHIRJSONOutput()
//                .addDocumentOutput()
//                .build());
        new File("liwei_out").mkdirs();
        goldPipeline.add(AnalysisEngineFactory.createEngineDescription(XMIWriterPostProcessor.class,
                XMIWriterPostProcessor.PARAM_OUTPUT_DIR, "liwei_out" // Don't need to create an output subdir, consumer does for us already
        ));
        SimplePipeline.runPipeline(cr, goldPipeline.createAggregateDescription());
//        // Merge gold standard into the extracted pipeline under the "Gold Standard" view and perform evaluation
//        CollectionReaderDescription evalCr = CollectionReaderFactory.createReaderDescription(EvaluationXMIReader.class,
//                EvaluationXMIReader.PARAM_EXTRACTED_INPUT, "out/xmi",
//                EvaluationXMIReader.PARAM_GOLD_INPUT, "gold_out/xmi"
//        );
//        AggregateBuilder evalPipeline = new AggregateBuilder();
//        evalPipeline.add(EvaluationPipelineBuilder.newBuilder()
//                .addIdentifierMatch(Condition.class, EvaluationPipelineBuilder.MatchType.POSITION, "Condition.code")
//                .addNewPositionMatches(Condition.class,
//                        "Condition.bodySite",
//                        "Condition.evidence",
//                        "Condition.severity")
//                .addNewValueMatches(Condition.class, "Condition.abatementString.value")
//                .addIdentifierMatch(Procedure.class, EvaluationPipelineBuilder.MatchType.POSITION, "Procedure.code")
//                .addNewPositionMatches(Procedure.class,
//                        "Procedure.bodySite")
//                .addIdentifierMatch(FamilyMemberHistory.class, EvaluationPipelineBuilder.MatchType.POSITION, "FamilyMemberHistory.condition.code")
//                .addNewValueMatches(FamilyMemberHistory.class, "FamilyMemberHistory.condition.note.text.value")
//                .build());
//        evalPipeline.add(SerializationPipelineBuilder
//                .newBuilder(new File("eval_out"))
//                .addXMIOutput()
//                .build());
//        SimplePipeline.runPipeline(evalCr, evalPipeline.createAggregateDescription());
    }
}