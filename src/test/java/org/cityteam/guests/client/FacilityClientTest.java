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

import org.cityteam.guests.action.ImportRequest;
import org.cityteam.guests.action.ImportResults;
import org.cityteam.guests.model.Facility;
import org.cityteam.guests.model.Guest;
import org.cityteam.guests.model.Registration;
import org.cityteam.guests.model.Template;
import org.cityteam.guests.model.types.FeatureType;
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.cityteam.guests.model.types.PaymentType.$$;
import static org.cityteam.guests.model.types.PaymentType.AG;
import static org.cityteam.guests.model.types.PaymentType.CT;
import static org.cityteam.guests.model.types.PaymentType.MM;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;

public class FacilityClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final FacilityClient facilityClient = new FacilityClient();

    private final RegistrationClient registrationClient =
            new RegistrationClient();

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

        List<Facility> facilities = facilityClient.findAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        for (Facility facility : facilities) {

            // Delete and verify we can no longer retrieve it
            facilityClient.delete(facility.getId());
            assertThrows(NotFound.class,
                    () -> facilityClient.find(facility.getId()));
        }

        assertThat(facilityClient.findAll().size(), is(0));

    }

    @Test
    public void deleteNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> facilityClient.delete(Long.MAX_VALUE));

    }

    // deleteRegistrationsByFacilityAndDate() tests

    @Test
    public void deleteRegistrationsByFacilityAndDate() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Chester");
        LocalDate registrationDate = LocalDate.parse("2020-07-04");
        List<Registration> registrations =
                facilityClient.findRegistrationsByFacilityAndDate
                        (facility.getId(), registrationDate);
        assertThat(registrations.size(), is(greaterThan(0)));

        for (Registration registration : registrations) {
            if (registration.getGuestId() != null) {
                registrationClient.deassign(registration.getId());
            }
        }

        List<Registration> results =
                facilityClient.deleteRegistrationsByFacilityAndDate
                        (facility.getId(), registrationDate);
        assertThat(results.size(), is(equalTo(registrations.size())));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Facility> facilities = facilityClient.findAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        for (Facility facility : facilities) {
            Facility found = facilityClient.find(facility.getId());
            assertThat(found.equals(facility), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> facilityClient.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Facility> facilities = facilityClient.findAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        String previousName = null;
        for (Facility facility : facilities) {
            String thisName = facility.getName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    // findByName() tests

    @Test
    public void findByNameHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Facility> facilities = facilityClient.findByName("san");
        assertThat(facilities.size(), is(greaterThan(0)));

        String previousName = null;
        for (Facility facility : facilities) {
            String thisName = facility.getName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    @Test
    public void findByNameNoMatch() throws Exception {

        if (disabled()) {
            return;
        }

        List<Facility> facilities = facilityClient.findByName("unmatched");
        assertThat(facilities.size(), is(0));

    }

    // findByNameExact() tests

    @Test
    public void findByNameExactHappy() throws Exception {

        if (disabled()) {
            return;
        }

        List<Facility> facilities = facilityClient.findAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        for (Facility facility : facilities) {
            Facility found = facilityClient.findByNameExact(facility.getName());
            assertThat(found.getId(), is(facility.getId()));
            assertThat(found.getName(), is(facility.getName()));
        }

    }

    @Test
    public void findByNameNotFound() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> facilityClient.findByNameExact("unmatched"));

    }

    // findGuestsByFacilityId() tests

    @Test
    public void findGuestsByFacilityIdHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        List<Guest> guests = facilityClient.findGuestsByFacilityId(facility.getId());
        assertThat(guests.size(), is(greaterThan(0)));

        String previousName = null;
        for (Guest guest : guests) {
            String thisName = guest.getLastName() + "|" + guest.getFirstName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    @Test
    public void findGuestsByFacilityIdNoMatch() throws Exception {

        if (disabled()) {
            return;
        }

        List<Guest> guests = facilityClient.findGuestsByFacilityId(Long.MAX_VALUE);
        assertThat(guests.size(), is(equalTo(0)));

    }

    // findGuestsByName() tests

    @Test
    public void findGuestsByNameHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        List<Guest> guests = facilityClient.findGuestsByName(facility.getId(), "ubble");
        assertThat(guests.size(), is(greaterThan(0)));

        String previousName = null;
        for (Guest guest : guests) {
            String thisName = guest.getLastName() + "|" + guest.getFirstName();
            if (previousName != null) {
                assertThat(thisName, is(greaterThan(previousName)));
            }
            previousName = thisName;
        }

    }

    @Test
    public void findGuestsByNameNoMatch() throws Exception {

        if (disabled()) {
            return;
        }

        List<Guest> guests = facilityClient.findGuestsByName(Long.MAX_VALUE, "ubble");
        assertThat(guests.size(), is(equalTo(0)));

    }

    // findGuestsByNameExact() tests

    @Test
    public void findGuestsByNameExactHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        Guest guest = facilityClient.findGuestsByNameExact
                (facility.getId(), "Fred", "Flintstone");

    }

    @Test
    public void findGuestsByNameExactNoMatch() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> facilityClient.findGuestsByNameExact
                        (Long.MAX_VALUE, "Fred", "Flintstone"));

    }

    // findRegistrationsByFacilityAndDate() tests

    @Test
    public void findRegistrationsByFacilityAndDate() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        List<Registration> registrations =
                facilityClient.findRegistrationsByFacilityAndDate
                        (facility.getId(), LocalDate.parse("2020-07-04"));
        assertThat(registrations.size(), is(greaterThan(0)));

    }

    // findTemplatesByFacilityId() tests

    @Test
    public void findTemplatesByFacilityIdHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        List<Template> templates =
                facilityClient.findTemplatesByFacilityId(facility.getId());
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

    @Test
    public void findTemplatesByFacilityIdNoMatch() throws Exception {

        if (disabled()) {
            return;
        }

        List<Template> templates =
                facilityClient.findTemplatesByFacilityId(Long.MAX_VALUE);
        assertThat(templates.size(), is(equalTo(0)));

    }

    // findTemplatesByName() tests

    @Test
    public void findTemplatesByNameHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        List<Template> templates =
                facilityClient.findTemplatesByName(facility.getId(), "land");
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

    @Test
    public void findTemplatesByNameNoMatch() throws Exception {

        if (disabled()) {
            return;
        }

        List<Template> templates =
                facilityClient.findTemplatesByName(Long.MAX_VALUE, "land");
        assertThat(templates.size(), is(equalTo(0)));

    }

    // findTemplatesByNameExact() tests

    @Test
    public void findTemplatesByNameExactHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        Template template = facilityClient.findTemplatesByNameExact
                (facility.getId(), "Oakland COVID");

    }

    @Test
    public void findTemplatesByNameExactNoMatch() throws Exception {

        if (disabled()) {
            return;
        }

        assertThrows(NotFound.class,
                () -> facilityClient.findTemplatesByNameExact
                        (Long.MAX_VALUE, "Unmatched"));

    }

    // importByFacilityAndDate() tests

    @Test
    public void importByFacilityAndDate() throws Exception {

        if (disabled()) {
            return;
        }

        // Accumulate information we need to perform this test

        String facilityName = "San Jose";
        Facility facility = facilityClient.findByNameExact(facilityName);
        LocalDate registrationDate = LocalDate.parse("2020-07-06");
        LocalTime showerTime = LocalTime.parse("04:00");
        LocalTime wakeupTime = LocalTime.parse("03:30");

        List<FeatureType> features1 =
                List.of(FeatureType.H);
        List<FeatureType> features2 =
                List.of(FeatureType.S);
        List<FeatureType> features3 =
                List.of(FeatureType.H, FeatureType.S);
        List<ImportRequest> imports = new ArrayList<>();

        // Add some unassigned mats
        imports.add(new ImportRequest(features1, 1));
        imports.add(new ImportRequest(features2, 2));
        imports.add(new ImportRequest(features3, 3));

        // Add some assigned mats (existing people)
        imports.add(new ImportRequest(
                "Fred on Mat 4",
                features1,
                "Fred",
                "Flintstone",
                4,
                null,
                AG,
                showerTime,
                null
        ));
        imports.add(new ImportRequest(
                "Bam Bam on Mat 5",
                features2,
                "Bam Bam",
                "Rubble",
                5,
                null,
                $$,
                null,
                wakeupTime
        ));
        imports.add(new ImportRequest(
                "Barney on Mat 6",
                features3,
                "Barney",
                "Rubble",
                6,
                null,
                MM,
                showerTime,
                wakeupTime
        ));

        // Add a new guest
        imports.add(new ImportRequest(
                "New Person on Mat 7",
                null,
                "New",
                "Person",
                7,
                null,
                CT,
                null,
                null
        ));

        // Import these and verify the results
        ImportResults importResults =
                facilityClient.importRegistrationsByFacilityAndDate(
                        facility.getId(),
                        registrationDate,
                        imports
                );
        assertThat(importResults.getRegistrations().size(),
                is(equalTo(imports.size())));

        // Retrieve them again and match them up
        List<Registration> retrieves =
                facilityClient.findRegistrationsByFacilityAndDate(
                        facility.getId(),
                        registrationDate
                );
        assertThat(retrieves.size(),
                is(equalTo(importResults.getRegistrations().size())));
        for (int i = 0; i < retrieves.size(); i++) {
            assertThat(retrieves.get(i),
                    is(equalTo(importResults.getRegistrations().get(i))));
        }

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = newFacility();
        Facility inserted = facilityClient.insert(facility);

        assertThat(inserted.getId(), is(notNullValue()));
        assertThat(inserted.getPublished(), is(notNullValue()));
        assertThat(inserted.getUpdated(), is(notNullValue()));
        assertThat(inserted.getVersion(), is(0));
        assertThat(inserted.getName(), is(facility.getName()));

    }

    @Test
    public void insertBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        // Completely empty instance
        final Facility facility0 = new Facility();
        assertThrows(BadRequest.class,
                () -> facilityClient.insert(facility0));

        // Missing name field
        final Facility facility1 = newFacility();
        facility1.setName(null);
        assertThrows(BadRequest.class,
                () -> facilityClient.insert(facility1));

    }

    @Test
    public void insertNotUnique() throws Exception {

        if (disabled()) {
            return;
        }

        Facility facility = facilityClient.findByNameExact("Oakland");
        assertThrows(NotUnique.class,
                () -> facilityClient.insert(facility));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        if (disabled()) {
            return;
        }

        // Change something but keep the name
        Facility facility1 = facilityClient.findByNameExact("Oakland");
        facility1.setCity(facility1.getCity() + " Updated");
        facilityClient.update(facility1.getId(), facility1);

        // Change name to something unique
        Facility facility2 = facilityClient.findByNameExact("Chester");
        facility2.setName("Unique Name");
        facilityClient.update(facility2.getId(), facility2);

    }

    @Test
    public void updateBadRequest() throws Exception {

        if (disabled()) {
            return;
        }

        // Required field
        Facility facility1 = facilityClient.findByNameExact("San Francisco");
        facility1.setName(null);
        assertThrows(BadRequest.class,
                () -> facilityClient.update(facility1.getId(), facility1));

    }

    @Test
    public void updateNotUnique() throws Exception {

        if (disabled()) {
            return;
        }

        // Violate name uniqueness
        Facility facility1 = facilityClient.findByNameExact("Oakland");
        facility1.setName("San Jose");
        assertThrows(NotUnique.class,
                () -> facilityClient.update(facility1.getId(), facility1));

    }

    // Support Methods -------------------------------------------------------

    private Facility newFacility() {
        return new Facility(
                "123 New Street",
                null,
                "New City",
                "newcity@cityteam.org",
                "New City",
                "999-555-1212",
                "US",
                "99999"
        );
    }

}
