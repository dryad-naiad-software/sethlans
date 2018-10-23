create table access_key (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, access_key varchar(255), primary key (id));
create table blender_benchmark_task (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, benchmark_dir varchar(255), benchmark_file varchar(255), benchmarkurl varchar(255), benchmarkuuid varchar(255), blender_executable varchar(255), blender_version varchar(255), complete boolean not null, compute_type integer, connectionuuid varchar(255), cpu_rating integer not null, deviceid varchar(255), gpu_rating integer not null, in_progress boolean not null, primary key (id));
create table blender_binary (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, blender_binaryos varchar(255), blender_file varchar(255), blender_file_md5 varchar(255), blender_version varchar(255), downloaded boolean not null, primary key (id));
create table blender_binary_download_mirrors (blender_binary_id bigint not null, download_mirrors varchar(255));
create table blender_project (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, all_images_processed boolean not null, blend_file_location varchar(255), blend_filename varchar(255), blend_filenamemd5sum varchar(255), blender_engine integer, blender_version varchar(255), completed_frames integer not null, current_frame_thumbnail varchar(255), current_percentage integer not null, end_frame integer not null, frame_rate varchar(255), movie_file_location varchar(255), parts_per_frame integer not null, project_end bigint, project_name varchar(255), project_root_dir varchar(255), project_start bigint, project_status integer, project_type integer, projectuuid varchar(255), queue_fill_complete boolean not null, queue_index integer not null, re_encode boolean not null, remaining_queue_size integer not null, render_on integer, render_output_format integer, res_percentage integer not null, resolutionx integer not null, resolutiony integer not null, samples integer not null, start_frame integer not null, step_frame integer not null, total_number_of_frames integer not null, total_project_time bigint, total_queue_size integer not null, total_render_time bigint, user_stopped boolean not null, sethlans_user_id bigint, primary key (id));
create table blender_project_frame_file_names (blender_project_id bigint not null, frame_file_names varchar(255));
create table frame_file_update_item (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, current_frame_thumbnail varchar(255), frame_file_name varchar(255), projectuuid varchar(255), primary key (id));
create table process_frame_item (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, frame_number integer, projectuuid varchar(255), primary key (id));
create table process_queue_item (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, connectionuuid varchar(255), part varchar(255), projectuuid varchar(255), queueuuid varchar(255), render_time bigint not null, primary key (id));
create table render_queue_item (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, combined boolean not null, file_extension varchar(255), frame_file_name varchar(255), frame_number integer not null, part_filename varchar(255), part_number integer not null, part_position_maxx double, part_position_maxy double, part_position_minx double, part_position_miny double, processed boolean not null, stored_dir varchar(255), complete boolean not null, connectionuuid varchar(255), gpu_device_id varchar(255), paused boolean not null, project_index integer not null, project_name varchar(255), projectuuid varchar(255), queue_itemuuid varchar(255), render_compute_type integer, rendering boolean not null, primary key (id));
create table render_task (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, blend_filemd5sum varchar(255), blend_filename varchar(255), blender_engine integer, blender_executable varchar(255), combined boolean not null, file_extension varchar(255), frame_file_name varchar(255), frame_number integer not null, part_filename varchar(255), part_number integer not null, part_position_maxx double, part_position_maxy double, part_position_minx double, part_position_miny double, processed boolean not null, stored_dir varchar(255), blender_version varchar(255), complete boolean not null, compute_type integer, connectionuuid varchar(255), deviceid varchar(255), in_progress boolean not null, part_res_percentage integer not null, project_name varchar(255), projectuuid varchar(255), render_dir varchar(255), render_output_format integer, render_taskuuid varchar(255), render_time bigint, samples integer not null, server_queueuuid varchar(255), task_resolutionx integer not null, task_resolutiony integer not null, primary key (id));
create table render_task_history (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, blend_file_name varchar(255), completed boolean not null, compute_type integer, deviceids varchar(255), engine integer, failed boolean not null, frame_and_part_numbers varchar(255), project_name varchar(255), render_taskuuid varchar(255), server_name varchar(255), task_date bigint, primary key (id));
create table sethlans_node (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, active boolean not null, allgpuslot_in_use boolean not null, available_rendering_slots integer not null, benchmark_complete boolean not null, combined boolean not null, compute_type integer, connectionuuid varchar(255), cpu_rating integer not null, cpu_slot_in_use boolean not null, arch varchar(255), cores integer not null, family varchar(255), model varchar(255), name varchar(255), total_memory varchar(255), disabled boolean not null, hostname varchar(255), ip_address varchar(255), network_port varchar(255), pending_activation boolean not null, selected_cores varchar(255), sethlans_nodeos integer, total_rendering_slots integer not null, primary key (id));
create table sethlans_node_selected_deviceid (sethlans_node_id bigint not null, selected_deviceid varchar(255));
create table sethlans_node_selectedgpumodels (sethlans_node_id bigint not null, selectedgpumodels varchar(255));
create table sethlans_node_selectedgpuratings (sethlans_node_id bigint not null, selectedgpuratings integer);
create table sethlans_node_selectedgpus (sethlans_node_id bigint not null, cuda boolean not null, deviceid varchar(255), in_use boolean not null, memory bigint not null, model varchar(255), opencl boolean not null, rating integer not null);
create table sethlans_notification (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, acknowledged boolean not null, link_present boolean not null, mailable boolean not null, message varchar(255), message_date bigint, message_link varchar(255), notification_type integer, scope integer, subject varchar(255), username varchar(255), primary key (id));
create table sethlans_server (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, acknowledged boolean not null, connectionuuid varchar(255), hostname varchar(255), ip_address varchar(255), network_port varchar(255), node_updated boolean not null, pending_acknowledgement_response boolean not null, primary key (id));
create table sethlans_user (id bigint generated by default as identity, date_created timestamp, last_updated timestamp, version bigint, active boolean not null, email varchar(255), node_email_notifications boolean not null, password varchar(255), project_email_notifications boolean not null, prompt_password_change boolean not null, security_questions_set boolean not null, system_email_notifications boolean not null, username varchar(255), video_encoding_email_notifications boolean not null, welcome_email_sent boolean not null, primary key (id));
create table sethlans_user_challenge_list (sethlans_user_id bigint not null, challenge varchar(255), response varchar(255));
create table sethlans_user_roles (sethlans_user_id bigint not null, roles integer);
create table sethlans_user_token_list (sethlans_user_id bigint not null, token_list varchar(255));
alter table blender_binary_download_mirrors add constraint FKoq7444qfspxba8fkwiro31wvn foreign key (blender_binary_id) references blender_binary;
alter table blender_project add constraint FKbp8botdmr0gmen99sukox59to foreign key (sethlans_user_id) references sethlans_user;
alter table blender_project_frame_file_names add constraint FKnpx04g0jejejx8x5ccg3j87tl foreign key (blender_project_id) references blender_project;
alter table sethlans_node_selected_deviceid add constraint FKjom4qtif35nqi5566t3guytmt foreign key (sethlans_node_id) references sethlans_node;
alter table sethlans_node_selectedgpumodels add constraint FKsmimeb6i28w7w2bips90ew9n foreign key (sethlans_node_id) references sethlans_node;
alter table sethlans_node_selectedgpuratings add constraint FK43n34rtw7e87nk6fnqplvmvs0 foreign key (sethlans_node_id) references sethlans_node;
alter table sethlans_node_selectedgpus add constraint FKdoxeti3evpj2j50a50wat78jh foreign key (sethlans_node_id) references sethlans_node;
alter table sethlans_user_challenge_list add constraint FKefber78m1dajh34h6srggt121 foreign key (sethlans_user_id) references sethlans_user;
alter table sethlans_user_roles add constraint FKok8vpag931sley6gweoc7a5jq foreign key (sethlans_user_id) references sethlans_user;
alter table sethlans_user_token_list add constraint FKr87yxdewgm8iww0lqgi3dps7e foreign key (sethlans_user_id) references sethlans_user;