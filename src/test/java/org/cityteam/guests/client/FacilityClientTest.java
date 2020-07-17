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
import org.craigmcc.library.shared.exception.BadRequest;
import org.craigmcc.library.shared.exception.NotFound;
import org.craigmcc.library.shared.exception.NotUnique;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.lang.Boolean.TRUE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.fail;

public class FacilityClientTest extends AbstractClientTest {

    // Instance Variables ----------------------------------------------------

    private final FacilityClient facilityClient = new FacilityClient();

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

        assertThrows(NotFound.class,
                () -> facilityClient.delete(Long.MAX_VALUE));

    }

    // find() tests

    @Test
    public void findHappy() throws Exception {

        List<Facility> facilities = facilityClient.findAll();
        assertThat(facilities.size(), is(greaterThan(0)));

        for (Facility facility : facilities) {
            Facility found = facilityClient.find(facility.getId());
            assertThat(found.equals(facility), is(true));
        }

    }

    @Test
    public void findNotFound() throws Exception {

        assertThrows(NotFound.class,
                () -> facilityClient.find(Long.MAX_VALUE));

    }

    // findAll() tests

    @Test
    public void findAllHappy() throws Exception {

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

        List<Facility> facilities = facilityClient.findByName("unmatched");
        assertThat(facilities.size(), is(0));

    }

    // findByNameExact() tests

    @Test
    public void findByNameExactHappy() throws Exception {

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

        assertThrows(NotFound.class,
                () -> facilityClient.findByNameExact("unmatched"));

    }

    // insert() tests

    @Test
    public void insertHappy() throws Exception {

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

        Facility facility = facilityClient.findByNameExact("Portland");
        assertThrows(NotUnique.class,
                () -> facilityClient.insert(facility));

    }

    // update() tests

    @Test
    public void updateHappy() throws Exception {

        // Change something but keep the name
        Facility facility1 = facilityClient.findByNameExact("Portland");
        facility1.setCity(facility1.getCity() + " Updated");
        facilityClient.update(facility1.getId(), facility1);

        // Change name to something unique
        Facility facility2 = facilityClient.findByNameExact("Chester");
        facility2.setName("Unique Name");
        facilityClient.update(facility2.getId(), facility2);

    }

    @Test
    public void updateBadRequest() throws Exception {

        // Required field
        Facility facility1 = facilityClient.findByNameExact("San Francisco");
        facility1.setName(null);
        assertThrows(BadRequest.class,
                () -> facilityClient.update(facility1.getId(), facility1));

    }

    @Test
    public void updateNotUnique() throws Exception {

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
