/*
 * Copyright 2020 CityTeam, craigmcc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cityteam.guests.client;

import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.Template;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.InternalServerError;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.cityteam.guests.model.types.FeatureType.H;
import static org.cityteam.guests.model.types.FeatureType.S;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThrows;

public class TemplateClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final FacilityClient facilityClient = new FacilityClient();

    private final TemplateClient templateClient = new TemplateClient();

    // Lifecycle Methods -----------------------------------------------------

    @Before
    public void before() {
        if ((depopulateEnabled == null) || (TRUE == depopulateEnabled)) {
            depopulate();
        }
        if ((populateEnabled == null) || (TRUE == populateEnabled)) {
            populate();
        }
    }

    // Test Methods ----------------------------------------------------------

    // delete() tests

    @Test
    public void deleteHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Template> templates = templateClient.findAll();
        assertThat(templates.size(), is(greaterThan(0)));

        for (Template template : templates) {

            // Delete and verify we can no longer retrieve it
            templateClient.delete(template.getId());
            assertThrows(NotFound.class,
                    () -> templateClient.find(template.getId()));
        }

        assertThat(templateClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> templateClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Template> templates = templateClient.findAll();
        assertThat(templates.size(), is(greaterThan(0)));

        for (Template template : templates) {
            Template found = templateClient.find(template.getId());
            assertThat(found.equals(template), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> templateClient.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Template> templates = templateClient.findAll();
        assertThat(templates.size(), is(greaterThan(0)));

        String previousName = null;
        for (Template template : templates) {
            String thisName = template.getName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    // generate() tests

    @Test
    public void generateHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("San Francisco");
        Template template = findTemplatesByNameExact(facility.getId(),
                "San Francisco COVID");

        LocalDate registrationDate = LocalDate.parse("2020-07-07");
        List<Registration> registrations = templateClient.generate
                (template.getId(), registrationDate);
        assertThat(registrations.size(), is(equalTo(12)));

        for (Registration registration : registrations) {
            assertThat(registration.getComments(), is(nullValue()));
            assertThat(registration.getFacilityId(),
                    is(equalTo(facility.getId())));
            if (registration.getMatNumber() == 1) {
                assertThat(registration.getFeatures().contains(H),
                        is(true));
            } else if (registration.getMatNumber() == 3) {
                assertThat(registration.getFeatures().contains(H),
                        is(true));
                assertThat(registration.getFeatures().contains(S),
                        is(true));
            } else if (registration.getMatNumber() == 5) {
                assertThat(registration.getFeatures().contains(S),
                        is(true));
            } else {
                assertThat(registration.getFeatures(), is(nullValue()));
            }
            assertThat(registration.getGuestId(), is(nullValue()));
            assertThat(registration.getMatNumber(), is(notNullValue()));
            assertThat(registration.getPaymentAmount(), is(nullValue()));
            assertThat(registration.getPaymentType(), is(nullValue()));
            assertThat(registration.getShowerTime(), is(nullValue()));
            assertThat(registration.getWakeupTime(), is(nullValue()));
            assertThat(registration.getRegistrationDate(),
                    is(equalTo(registrationDate)));
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("San Francisco");
        Template template = newTemplate(facility.getId());
        Template inserted = templateClient.insert(template);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(inserted.getName(), is(template.getName()));

    }

    @Test
    public void insertBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("San Jose");

        // Completely empty instance
        final Template template0 = new Template();
        assertThrows(BadRequest.class,
                () -> templateClient.insert(template0));

        // Missing name field
        final Template template1 = newTemplate(facility.getId());
        template1.setName(null);
        assertThrows(BadRequest.class,
                () -> templateClient.insert(template1));

    }

    @Test
    public void insertNotUnique() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("San Jose");
        Template template = newTemplate(facility.getId());
        template.setName("San Jose COVID");
        assertThrows(NotUnique.class,
                () -> templateClient.insert(template));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");

        // Change something but keep the name
        Template template1 =
                facilityClient.findTemplatesByNameExact
                        (facility.getId(), "Oakland COVID");
        template1.setComments("Updated Comments");
        templateClient.update(template1.getId(), template1);

        // Change name to something unique
        Template template2 =
                facilityClient.findTemplatesByNameExact
                        (facility.getId(), "Oakland Standard");
        template2.setName("Unique Name");
        templateClient.update(template2.getId(), template2);

    }

    @Test
    public void updateBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Chester");

        // Required field
        Template template1 =
                facilityClient.findTemplatesByNameExact
                        (facility.getId(), "Chester COVID");
        template1.setName(null);
        assertThrows(BadRequest.class,
                () -> templateClient.update(template1.getId(), template1));

    }

    @Test
    public void updateNotUnique() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Chester");

        // Violate name uniqueness
        Template template1 =
                facilityClient.findTemplatesByNameExact
                        (facility.getId(), "Chester COVID");
        template1.setName("Chester Standard");
        assertThrows(NotUnique.class,
                () -> templateClient.update(template1.getId(), template1));

    }

    // Support Methods -------------------------------------------------------

    private Template findTemplatesByNameExact(Long facilityId, String name)
        throws InternalServerError, NotFound
    {
        return facilityClient.findTemplatesByNameExact(facilityId, name);
    }

    private Template newTemplate(Long facilityId) {
        return new Template(
                "1-100",
                null,
                facilityId,
                null,
                "New Template",
                null
        );
    }

}
