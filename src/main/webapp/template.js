$(document).ready(function() {
    // Validate if the student ID contains only digits
    function isValidStudentId(id) {
        return /^\d+$/.test(id);
    }

    // Validate that the Requested Extension date is greater than the original deadline
    function areDatesValid(originalDeadline, requestedExtension) {
        return requestedExtension > originalDeadline;
    }

    // Function to attach date validation logic for module items
    function attachDateValidation(moduleItem) {
        $(moduleItem).find('.RequestedExtension, .originalDeadline').on('change', function() {
            var originalDeadlineStr = $(moduleItem).find('.originalDeadline').val();
            var requestedExtensionStr = $(moduleItem).find('.RequestedExtension').val();

            if (originalDeadlineStr && requestedExtensionStr) {
                var originalDeadline = new Date(originalDeadlineStr);
                var requestedExtension = new Date(requestedExtensionStr);
                
                if (!areDatesValid(originalDeadline, requestedExtension)) {
                    alert('Requested Extension must be after the Original Deadline.');
                    $(this).val(''); // Clear the invalid date
                }
            }
        });
    }

    // Validate existing dates on initial load
    function validateExistingDates() {
        $('.module-item').each(function() {
            attachDateValidation(this); // Attach validation to each existing module item
        });

        // Also validate initial form fields
        var initialOriginalDeadlineStr = $('.originalDeadline').val();
        var initialRequestedExtensionStr = $('.RequestedExtension').val();

        if (initialOriginalDeadlineStr && initialRequestedExtensionStr) {
            var initialOriginalDeadline = new Date(initialOriginalDeadlineStr);
            var initialRequestedExtension = new Date(initialRequestedExtensionStr);
            
            if (!areDatesValid(initialOriginalDeadline, initialRequestedExtension)) {
                alert('Requested Extension must be after the Original Deadline in the initial form.');
                $('.RequestedExtension').val(''); // Clear invalid date
            }
        }
    }

    // Handle Student ID validation and data fetching
    $('#studentId').on('blur', function() {
        var studentId = $(this).val();
        
        if (!isValidStudentId(studentId)) {
            alert('Student ID must be a valid integer.');
            $(this).val('');
            $('#studentFirstName').val('');
            $('#studentLastName').val('');
            $('#studentEmail').val('');
            return;
        }

        if (studentId) {
            $.ajax({
                url: 'GetStudentDetailsServlet',
                type: 'POST',
                dataType: 'json',
                data: { studentId: studentId },
                success: function(response) {
                    if (response.success) {
                        $('#studentFirstName').val(response.firstName);
                        $('#studentLastName').val(response.lastName);
                        $('#studentEmail').val(response.email);
                    } else {
                        alert(response.message);
                    }
                },
                error: function() {
                    alert('Error fetching student details');
                }
            });
        }
    });

    // Handle adding a new module request form
    window.addModule = function() {
        $.ajax({
            url: 'ModuleAndCourseworkServlet',
            type: 'GET',
            dataType: 'json',
            success: function(response) {
                if (response.success) {
                    const container = $('#module-container');
                    const moduleItem = $(`
                        <div class="module-item">
                            <div class="form-group">
                                <label for="moduleTitle">Module Title: <span class="required">*</span></label>
                                <select class="moduleTitle" name="moduleTitle" required>
                                    <option value="" disabled selected>Select Module</option>
                                    ${response.modules.map(module => `<option value="${module.moduleName}">${module.moduleName}</option>`).join('')}
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="courseworkTitle">Select Coursework: <span class="required">*</span></label>
                                <select class="courseworkSelect" name="courseworkTitle" required>
                                    <option value="" disabled selected>Select Coursework</option>
                                    ${response.courseworkTitles.map(title => `<option value="${title}">${title}</option>`).join('')}
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="originalDeadline">Original Deadline: <span class="required">*</span></label>
                                <input type="date" class="originalDeadline" name="originalDeadline" required>
                            </div>
                            <div class="form-group">
                                <label for="RequestedExtension">Requested Extension: <span class="required">*</span></label>
                                <input type="date" class="RequestedExtension" name="RequestedExtension" required>
                            </div>
                            <div class="form-group">
                                <label for="decision">Decision: <span class="required">*</span></label>
                                <select class="decision" name="decision" required>
                                    <option value="" disabled selected>Select Decision</option>
                                    <option value="Approved">Approved</option>
                                    <option value="Denied">Denied</option>
                                    <option value="Require Additional Documents">Require Additional Documents</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label for="comments">Comments:</label>
                                <textarea class="comments" name="comments"></textarea>
                            </div>
                        </div>
                    `);
                    
                    container.append(moduleItem);
                    
                    // Attach date validation for the newly added module
                    attachDateValidation(moduleItem);
                } else {
                    alert(response.message);
                }
            },
            error: function() {
                alert('Error fetching module and coursework details');
            }
        });
    };

    // Validate existing dates on initial load
    validateExistingDates();

    // Form submission validation
    $('#emailForm').on('submit', function(event) {
        var valid = true;
        var errorMessage = '';

        // Validate deadlines
        $('.module-item').each(function() {
            var originalDeadlineStr = $(this).find('.originalDeadline').val();
            var requestedExtensionStr = $(this).find('.RequestedExtension').val();

            if (originalDeadlineStr && requestedExtensionStr) {
                var originalDeadline = new Date(originalDeadlineStr);
                var requestedExtension = new Date(requestedExtensionStr);

                // Check if the requested extension is not earlier than or the same as the original deadline
                if (!areDatesValid(originalDeadline, requestedExtension)) {
                    errorMessage = 'Requested Extension must be after the Original Deadline.';
                    valid = false;
                    return false; 
                }
            }
        });

        // Validate Student ID format
        var studentId = $('#studentId').val();
        if (!isValidStudentId(studentId)) {
            errorMessage = 'Student ID must be a valid integer.';
            valid = false;
        }

        if (!valid) {
            alert(errorMessage);
            event.preventDefault(); // Prevent form submission
        }
    });
});
