$(document).ready(function() {
    $('#studentId').on('blur', function() {
        var studentId = $(this).val();
        
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

    window.addModule = function() {
        $.ajax({
            url: 'ModuleAndCourseworkServlet',
            type: 'GET',
            dataType: 'json',
            success: function(response) {
                if (response.success) {
                    const container = document.getElementById('module-container');
                    const moduleItem = document.createElement('div');
                    moduleItem.className = 'module-item';
                    
                    moduleItem.innerHTML = `
                        <label for="moduleTitle">Module Title: <span class="required">*</span></label>
                        <select class="moduleTitle" name="moduleTitle" required>
                            <option value="" disabled selected>Select Module</option>
                            ${response.modules.map(module => `<option value="${module.moduleName}">${module.moduleName}</option>`).join('')}
                        </select>
                        <div>
                            <label for="courseworkTitle">Select Coursework: <span class="required">*</span></label>
                            <select class="courseworkSelect" name="courseworkTitle" required>
                                <option value="" disabled selected>Select Coursework</option>
                                ${response.courseworkTitles.map(title => `<option value="${title}">${title}</option>`).join('')}
                            </select>
                        </div>
                        <div>
                            <label for="originalDeadline">Original Deadline: <span class="required">*</span></label>
                            <input type="date" class="originalDeadline" name="originalDeadline" required>
                        </div>
                        <div>
                            <label for="RequestedExtension">Requested Extension: <span class="required">*</span></label>
                            <input type="date" class="RequestedExtension" name="RequestedExtension" required>
                        </div>
                        <div>
                            <label for="decision">Decision: <span class="required">*</span></label>
                            <select class="decision" name="decision" required>
                                <option value="" disabled selected>Select Decision</option>
                                <option value="Approved">Approved</option>
                                <option value="Denied">Denied</option>
                            </select>
                        </div>
                        <div>
                            <label for="comments">Comments:</label>
                            <textarea class="comments" name="comments"></textarea>
                        </div>
                    `;
                    
                    container.appendChild(moduleItem);
                } else {
                    alert(response.message);
                }
            },
            error: function() {
                alert('Error fetching module and coursework details');
            }
        });
    }
    
});
