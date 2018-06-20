/*
 * Copyright 2017 Johns Hopkins University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dataconservancy.nihms.builder.fs;

import org.dataconservancy.nihms.model.DepositFile;
import org.dataconservancy.nihms.model.DepositFileType;
import org.dataconservancy.nihms.model.DepositMetadata;
import org.dataconservancy.nihms.model.DepositSubmission;

import org.dataconservancy.pass.model.PassEntity;
import org.dataconservancy.pass.model.Publication;
import org.dataconservancy.pass.model.Submission;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class FilesystemModelBuilderTest {

    private DepositSubmission submission;
    private FilesystemModelBuilder underTest = new FilesystemModelBuilder();
    private String SAMPLE_SUBMISSION_RESOURCE = "SampleSubmissionData.json";
    private String SAMPLE_SUBMISSION_RESOURCE_NULL_FIELDS = "/SampleSubmissionData-with-null-common-md-fields.json";
    private String SAMPLE_SUBMISSION_RESOURCE_NULL_DOI = "/SampleSubmissionData-null-doi.json";
    private String SAMPLE_SUBMISSION_RESOURCE_UNTRIMMED_DOI = "/SampleSubmissionData-untrimmed-doi.json";
    private String SAMPLE_SUBMISSION_RESOURCE_TABLE_AND_FIGURE = "/SampleSubmissionData-with-figure-and-table-files.json";

    @Before
    public void setup() throws Exception{
        // Create submission data from sample data file
        URL sampleDataUrl = FilesystemModelBuilderTest.class.getClassLoader().getResource(SAMPLE_SUBMISSION_RESOURCE);
        submission = underTest.build(sampleDataUrl.getPath());
    }

    @Test
    public void testElementValues() {
        // Load the PassEntity version of the sample data file
        Submission submissionEntity = null;
        HashMap<URI, PassEntity> entities = new HashMap<>();
        URL sampleDataUrl = FilesystemModelBuilderTest.class.getClassLoader().getResource(SAMPLE_SUBMISSION_RESOURCE);
        String sampleDataPath = sampleDataUrl.getPath();
        try {
            InputStream is = new FileInputStream(sampleDataPath);
            PassJsonFedoraAdapter reader = new PassJsonFedoraAdapter();
            submissionEntity = reader.jsonToPass(is, entities);
            is.close();
        } catch (FileNotFoundException e) {
            fail(String.format("Could not load sample data file '%s'", sampleDataPath));
        } catch (IOException e) {
            fail(String.format("Could not close the sample data file '%s'", sampleDataPath));
        }

        // Check that some basic things are in order
        assertNotNull(submission.getManifest());
        assertNotNull(submission.getMetadata());
        assertNotNull(submission.getMetadata().getManuscriptMetadata());
        assertNotNull(submission.getMetadata().getJournalMetadata());
        assertNotNull(submission.getMetadata().getArticleMetadata());
        assertNotNull(submission.getMetadata().getPersons());

        assertEquals(submission.getId(), submissionEntity.getId().toString());
        Publication publication = (Publication)entities.get(submissionEntity.getPublication());
        assertEquals(submission.getMetadata().getArticleMetadata().getDoi().toString(), publication.getDoi());

        assertNotNull(submission.getFiles());
        assertEquals(2, submission.getFiles().size());

        // Confirm that some values were set correctly from the Submission metadata
        DepositMetadata.Journal journalMetadata = submission.getMetadata().getJournalMetadata();
        assertEquals("Food Funct.", journalMetadata.getJournalTitle());
        assertEquals("TD452689", journalMetadata.getJournalId());
        assertEquals("2042-6496,2042-650X", journalMetadata.getIssn());

        DepositMetadata.Manuscript manuscriptMetadata = submission.getMetadata().getManuscriptMetadata();
        assertEquals("http://dx.doi.org/10.1039/c7fo01251a", manuscriptMetadata.getManuscriptUrl().toString());

    }

    @Test
    public void buildWithNullValues() throws Exception {
        // Create submission data from sample data file with null values
        URL sampleDataUrl = this.getClass().getResource(SAMPLE_SUBMISSION_RESOURCE_NULL_FIELDS);
        assertNotNull("Could not resolve classpath resource " + SAMPLE_SUBMISSION_RESOURCE_NULL_FIELDS, sampleDataUrl);
        submission = underTest.build(sampleDataUrl.getPath());

        assertNotNull(submission);
        assertNull(submission.getMetadata().getManuscriptMetadata().getMsAbstract());
        assertNull(submission.getMetadata().getManuscriptMetadata().getManuscriptUrl());
    }

    @Test
    public void buildWithNullDoi() throws Exception {
        // Create submission data from sample data file with null values
        URL sampleDataUrl = this.getClass().getResource(SAMPLE_SUBMISSION_RESOURCE_NULL_DOI);
        assertNotNull("Could not resolve classpath resource " + SAMPLE_SUBMISSION_RESOURCE_NULL_DOI, sampleDataUrl);
        submission = underTest.build(sampleDataUrl.getPath());

        assertNotNull(submission);
        assertNull(submission.getMetadata().getArticleMetadata().getDoi());
    }

    @Test
    public void buildWithUntrimmedDoi() throws Exception {
        // Create submission data from sample data file with null values
        URL sampleDataUrl = this.getClass().getResource(SAMPLE_SUBMISSION_RESOURCE_UNTRIMMED_DOI);
        assertNotNull("Could not resolve classpath resource " +
                SAMPLE_SUBMISSION_RESOURCE_UNTRIMMED_DOI, sampleDataUrl);
        submission = underTest.build(sampleDataUrl.getPath());

        assertNotNull(submission);
        URI doi = submission.getMetadata().getArticleMetadata().getDoi();
        assertNotNull(doi);
        assertFalse(doi.toString().startsWith(" "));
        assertFalse(doi.toString().endsWith(" "));
    }

    @Test
    public void buildWithTableAndFigure() throws Exception {
        // Create submission data from sample data file with table and figure files
        URL sampleDataUrl = this.getClass().getResource(SAMPLE_SUBMISSION_RESOURCE_TABLE_AND_FIGURE);
        assertNotNull("Could not resolve classpath resource " + SAMPLE_SUBMISSION_RESOURCE_TABLE_AND_FIGURE, sampleDataUrl);
        submission = underTest.build(sampleDataUrl.getPath());

        assertNotNull(submission);
        assertNotNull(submission.getFiles());
        assertEquals(4, submission.getFiles().size());
        List<DepositFileType> types = new ArrayList<>();
        for (DepositFile file : submission.getFiles()) {
            types.add(file.getType());
        }

        assertTrue(types.contains(DepositFileType.figure));
        assertTrue(types.contains(DepositFileType.supplement));
        assertTrue(types.contains(DepositFileType.table));
        assertTrue(types.contains(DepositFileType.manuscript));
    }
}