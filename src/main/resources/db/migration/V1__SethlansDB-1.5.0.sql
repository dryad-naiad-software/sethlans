create sequence hibernate_sequence start with 1 increment by 1;
create table blender_archive
(
    id               bigint  not null,
    date_created     timestamp,
    last_updated     timestamp,
    version          bigint,
    blender_file     varchar(255),
    blender_file_md5 varchar(255),
    blenderos        integer,
    blender_version  varchar(255),
    downloaded       boolean not null,
    primary key (id)
);
create table node
(
    id                    bigint  not null,
    date_created          timestamp,
    last_updated          timestamp,
    version               bigint,
    active                boolean not null,
    benchmark_complete    boolean not null,
    arch                  varchar(255),
    cores                 integer,
    cpu_package           integer,
    family                varchar(255),
    model                 varchar(255),
    name                  varchar(255),
    total_memory          varchar(255),
    cpu_rating            integer,
    hostname              varchar(255),
    ip_address            varchar(255),
    network_port          varchar(255),
    node_type             integer,
    os                    integer,
    systemid              varchar(255),
    total_rendering_slots integer,
    primary key (id)
);
create table node_selectedgpus
(
    node_id     bigint not null,
    device_type integer,
    gpuid       varchar(255),
    memory      bigint,
    model       varchar(255),
    rating      bigint
);
create table project
(
    id                     bigint  not null,
    date_created           timestamp,
    last_updated           timestamp,
    version                bigint,
    projectid              varchar(255),
    project_name           varchar(255),
    project_root_dir       varchar(255),
    animation_type         integer,
    blend_file_location    varchar(255),
    blend_filename         varchar(255),
    blend_filenamemd5sum   varchar(255),
    blender_engine         integer,
    blender_version        varchar(255),
    compute_on             integer,
    end_frame              integer,
    image_output_format    integer,
    res_percentage         integer,
    resolutionx            integer,
    resolutiony            integer,
    parts_per_frame        integer,
    samples                integer,
    start_frame            integer,
    step_frame             integer,
    total_number_of_frames integer,
    use_parts              boolean not null,
    codec                  integer,
    frame_rate             integer,
    pixel_format           integer,
    video_file_location    varchar(255),
    video_output_format    integer,
    video_quality          integer,
    all_images_processed   boolean not null,
    completed_frames       integer,
    current_percentage     integer,
    project_state          integer,
    queue_fill_complete    boolean not null,
    queue_index            integer,
    re_encode              boolean not null,
    remaining_queue_size   integer,
    timer_end              bigint,
    timer_start            bigint,
    total_project_time     bigint,
    total_queue_size       integer,
    total_render_time      bigint,
    user_stopped           boolean not null,
    project_type           integer,
    user_id                bigint,
    primary key (id)
);
create table project_frame_file_names
(
    project_id       bigint not null,
    frame_file_names varchar(255)
);
create table project_frame_list
(
    project_id      bigint  not null,
    combined        boolean not null,
    file_extension  varchar(255),
    frame_name      varchar(255),
    frame_number    integer,
    parts_per_frame integer,
    stored_dir      varchar(255)
);
create table project_thumbnail_file_names
(
    project_id           bigint not null,
    thumbnail_file_names varchar(255)
);
create table render_task
(
    id                      bigint  not null,
    date_created            timestamp,
    last_updated            timestamp,
    version                 bigint,
    benchmark               boolean not null,
    blender_executable      varchar(255),
    blender_version         varchar(255),
    cancel_request_received boolean not null,
    complete                boolean not null,
    frame_number            integer,
    part_maxx               double,
    part_maxy               double,
    part_minx               double,
    part_miny               double,
    part_number             integer,
    in_progress             boolean not null,
    projectid               varchar(255),
    project_name            varchar(255),
    render_time             bigint,
    blender_engine          integer,
    compute_on              integer,
    cores                   integer,
    device_type             integer,
    image_output_format     integer,
    samples                 integer,
    task_res_percentage     integer,
    task_resolutionx        integer,
    task_resolutiony        integer,
    task_tile_size          integer,
    server_queueid          varchar(255),
    systemid                varchar(255),
    task_blend_file         varchar(255),
    task_blend_filemd5sum   varchar(255),
    task_dir                varchar(255),
    taskid                  varchar(255),
    use_parts               boolean not null,
    primary key (id)
);
create table render_task_deviceids
(
    render_task_id bigint not null,
    deviceids      varchar(255)
);
create table server
(
    id           bigint not null,
    date_created timestamp,
    last_updated timestamp,
    version      bigint,
    hostname     varchar(255),
    ip_address   varchar(255),
    network_port varchar(255),
    systemid     varchar(255),
    primary key (id)
);
create table user
(
    id                                 bigint  not null,
    date_created                       timestamp,
    last_updated                       timestamp,
    version                            bigint,
    active                             boolean not null,
    email                              varchar(255),
    node_email_notifications           boolean not null,
    password                           varchar(255),
    password_updated                   boolean not null,
    project_email_notifications        boolean not null,
    prompt_password_change             boolean not null,
    security_questions_set             boolean not null,
    system_email_notifications         boolean not null,
    userid                             varchar(255),
    username                           varchar(255),
    video_encoding_email_notifications boolean not null,
    welcome_email_sent                 boolean not null,
    primary key (id)
);
create table user_challenge_list
(
    user_id          bigint  not null,
    challenge        varchar(255),
    response         varchar(255),
    response_updated boolean not null
);
create table user_roles
(
    user_id bigint not null,
    roles   integer
);
create table user_tokens
(
    user_id bigint not null,
    tokens  varchar(255)
);
alter table node_selectedgpus
    add constraint FKecl1m1xgun9u2m0hgdkf0igv9 foreign key (node_id) references node;
alter table project
    add constraint FKo06v2e9kuapcugnyhttqa1vpt foreign key (user_id) references user;
alter table project_frame_file_names
    add constraint FKdua5syfnvj1vfxoubcaabrexo foreign key (project_id) references project;
alter table project_frame_list
    add constraint FKlifif3dtu4hq9m37l4a6yjppl foreign key (project_id) references project;
alter table project_thumbnail_file_names
    add constraint FKiwudsv5vimn14ciimru3jennx foreign key (project_id) references project;
alter table render_task_deviceids add constraint FKj90ikv9jkqjje8cbf4tdurxlb foreign key (render_task_id) references render_task;
alter table user_challenge_list add constraint FKr851d51ro5a0we9puu6sg1fal foreign key (user_id) references user;
alter table user_roles add constraint FK55itppkw3i07do3h7qoclqd4k foreign key (user_id) references user;
alter table user_tokens add constraint FKcd1yoodas8b3t22j5jhd9vjvw foreign key (user_id) references user;