# Iteration 2 Test Case
Group: Group85
Tester: Jiayi Lou

| Test ID | Test Function | Steps | Expected Result | Actual Result | Pass |
| --- | --- | --- | --- | --- | --- |
| TC-001 | TA Registration | 1. Open system<br>2. Click Register<br>3. Enter valid info<br>4. Submit | Register successfully, data saved to users.json | Normal | Yes |
| TC-002 | TA Login | 1. Enter correct username & password<br>2. Click Login | Login successfully, enter TA homepage | Normal | Yes |
| TC-003 | TA Create Profile | 1. Login as TA<br>2. Fill in profile<br>3. Save | Profile saved to ta_profiles.json | Normal | Yes |
| TC-004 | TA View Jobs | 1. Login as TA<br>2. Enter job list page | All available jobs displayed | Normal | Yes |
| TC-005 | TA Apply for Job | 1. Select a job<br>2. Click Apply | Application submitted, status updated | Normal | Yes |
| TC-006 | TA Profile Data Edit & Update | 1. Save a complete profile<br>2. Modify existing information (e.g. update phone number, change available hours)<br>3. Click Save again | System updates the data successfully, ta_profiles.json is overwritten with new content, refresh shows the latest information | Normal | Yes |
| TC-007 | Invalid Login | Enter wrong password | Error message shown, login failed | Normal | Yes |
| TC-008 | TA Personal Information Fill & Save | 1. Login as TA, enter My Profile page<br>2. Fill in all required fields (* marked): Surname, Forename, Student ID, Phone Number, Gender, School, Student Type, Current Year, Campus<br>3. Fill in optional fields: Chinese Name, Major, Available Hours/Week<br>4. Click Save button | 1. All required fields can be input normally<br>2. System saves all input data successfully<br>3. Data is correctly written to ta_profiles.json<br>4. After refreshing the page, all filled information is retained | Normal | Yes |
| TC-009 | TA Profile Required Field Validation | 1. Enter My Profile page<br>2. Leave 1+ required fields (* marked) blank<br>3. Click Save | System pops up error prompt, blocks submission, and reminds to fill in missing required fields | Normal | Yes |